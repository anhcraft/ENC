package org.anhcraft.enc.api;

import org.anhcraft.enc.utils.DelayedRunnable;
import org.anhcraft.enc.utils.RomanNumber;
import org.anhcraft.spaciouslib.utils.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class EnchantmentAPI {
    private static final List<String> DEFAULT_WORLDS_LIST = CommonUtils.toList(new String[]{"world"});
    private static final String LORE_PREFIX = Chat.color("&2&6&7");
    private static final InitialisationValidator INIT_LOCK = new InitialisationValidator();
    private static final HashMap<String, Enchantment> ENCHANT_MAP = new HashMap<>();
    private ConfigurationSection config;
    private DelayedRunnable saver;

    public EnchantmentAPI(ConfigurationSection config, DelayedRunnable saver){
        try {
            INIT_LOCK.validate();
        } catch(Exception e) {
            e.printStackTrace();
        }
        this.config = config;
        this.saver = saver;
        applyEnchantmentConfigs();
    }

    /**
     * Applies changes for all configuration of enchantments.
     */
    public void applyEnchantmentConfigs() {
        ENCHANT_MAP.values().forEach(enchantment -> enchantment.initConfig(config
                .getConfigurationSection(enchantment.getId().toUpperCase())));
    }

    /**
     * Saves the enchantment configuration.<br>
     * This method is asynchronous since it is delayed.
     */
    public void saveEnchantmentConfig() {
        saver.run();
    }

    /**
     * Registers the given enchantment.
     * @param enchant the enchantment
     */
    public void registerEnchantment(Enchantment enchant) {
        String id = enchant.getId().toUpperCase();
        ExceptionThrower.ifTrue(ENCHANT_MAP.containsKey(id), new Exception("Enchantment is already registered: Id must be unique"));
        ExceptionThrower.ifFalse(ENCHANT_MAP.values().stream().noneMatch(enchantment ->
                enchantment.getName().equals(enchant.getName())), new Exception("Enchantment is already registered: Name must be unique"));
        ENCHANT_MAP.put(id, enchant);
        if(config.isSet(id)) {
            enchant.initConfig(config.getConfigurationSection(id));
        } else {
            // create the config if it does not exist
            ConfigurationSection section = new YamlConfiguration();
            section.set("enabled", true);
            section.set("chat_prefix", "&5#{lowercase_enchant_id} > &f");
            section.set("worlds_list", new ArrayList<>(DEFAULT_WORLDS_LIST));
            section.set("allowed_worlds_list", true);
            section.set("name", enchant.getId());
            config.set(id, section);
            enchant.initConfig(section);
            saveEnchantmentConfig();
        }
    }

    /**
     * Unregisters the given enchantment.
     * @param enchant the enchantment
     */
    public void unregisterEnchantment(Enchantment enchant) {
        String id = enchant.getId().toUpperCase();
        ExceptionThrower.ifFalse(ENCHANT_MAP.containsKey(id), new Exception("Enchantment is not registered yet"));
        ENCHANT_MAP.remove(id);
    }

    /**
     * Checks whether the given enchantment is registered.
     * @param enchant the enchantment
     * @return true if yes
     */
    public boolean isEnchantmentRegistered(Enchantment enchant) {
        return ENCHANT_MAP.containsValue(enchant);
    }

    /**
     * Gets the enchantment by its unique id.
     * @param enchantId the enchantment's id
     * @return the enchantment (may be null if it is not found)
     */
    public Enchantment getEnchantmentById(String enchantId) {
        return ENCHANT_MAP.get(enchantId.toUpperCase());
    }

    /**
     * Gets the enchantment by its name.<br>
     * The name can be coloured or not
     * @param enchantName the enchantment's name
     * @return the enchantment (may be null if it is not found)
     */
    public Enchantment getEnchantmentByName(String enchantName) {
        String x = Chat.color(enchantName);
        return ENCHANT_MAP.values().stream().filter(enchantment ->
                enchantment.getName().equals(x)).findFirst().orElse(null);
    }

    /**
     * Gets all registered enchantments.
     * @return an array of enchantments
     */
    public List<Enchantment> getRegisteredEnchantments() {
        return new ArrayList<>(ENCHANT_MAP.values());
    }

    /**
     * Gets coloured names of all registered enchantments.
     * @return an array of enchantment's coloured name
     */
    public List<String> getRegisteredEnchantmentNames() {
        return ENCHANT_MAP.values().stream().map(Enchantment::getName).collect(Collectors.toList());
    }

    /**
     * Gets ids of all registered enchantments.
     * @return an array of enchantment's id
     */
    public List<String> getRegisteredEnchantmentIds() {
        return ENCHANT_MAP.values().stream().map(Enchantment::getId).collect(Collectors.toList());
    }

    /**
     * Checks whether the given stack of items is enchanted by an enchantment.
     * @param enchant the enchantment
     * @param itemStack the stack of items
     * @return true if yes
     */
    public boolean isEnchanted(Enchantment enchant, ItemStack itemStack) {
        if(!InventoryUtils.isNull(itemStack)) {
            ItemMeta m = itemStack.getItemMeta();
            if(m.hasLore()) {
                for(String l : m.getLore()) {
                    if(l.startsWith(LORE_PREFIX + enchant.getName() + " ")) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Lists all enchantments of the given stack of item.
     * @param itemStack the stack of items
     * @return a map of enchantments which includes the name and the level
     */
    public HashMap<Enchantment, Integer> listEnchantments(ItemStack itemStack) {
        if(!InventoryUtils.isNull(itemStack)) {
            ItemMeta m = itemStack.getItemMeta();
            if(m.hasLore()) {
                HashMap<Enchantment, Integer> map = new HashMap<>();
                for(String l : m.getLore()) {
                    if(l.startsWith(LORE_PREFIX)) {
                        String[] x = l.substring(LORE_PREFIX.length()).split(" ");
                        String name = String.join(" ", Arrays.copyOfRange(x, 0, x.length-1));
                        int lv = RomanNumber.toDecimal(x[x.length-1]);
                        map.put(getEnchantmentByName(name), lv);
                    }
                }
                return map;
            }
        }
        return new HashMap<>();
    }

    /**
     * Gets the level of an existing enchantment.
     * @param itemStack a stack of items
     * @param enchant the enchantment
     * @return the enchantment level (may be 0 if the enchantment is not found)
     */
    public int getEnchantmentLevel(ItemStack itemStack, Enchantment enchant) {
        if(!InventoryUtils.isNull(itemStack)) {
            ItemMeta m = itemStack.getItemMeta();
            if(m.hasLore()) {
                for(String l : m.getLore()) {
                    if(l.startsWith(LORE_PREFIX + enchant.getName() + " ")) {
                        String[] x = l.substring(LORE_PREFIX.length()).split(" ");
                        return RomanNumber.toDecimal(x[x.length-1]);
                    }
                }
            }
        }
        return 0;
    }

    /**
     * Adds a certain enchantment to the given stack of items.
     * @param itemStack the stack of items
     * @param enchant the enchantment
     * @param level the level
     */
    public void addEnchantment(ItemStack itemStack, Enchantment enchant, int level) {
        if(!InventoryUtils.isNull(itemStack)) {
            ItemMeta m = itemStack.getItemMeta();
            List<String> lore = new ArrayList<>();
            lore.add(LORE_PREFIX + enchant.getName() + " " + RomanNumber.toRoman(level));
            if(m.hasLore()) {
                for(String l : m.getLore()) {
                    if(!l.startsWith(LORE_PREFIX + enchant.getName() + " ")) {
                        lore.add(l);
                    }
                }
            }
            m.setLore(lore);
            itemStack.setItemMeta(m);
        }
    }

    /**
     * Removes an existing enchantment out of the given stack of items.
     * @param itemStack the stack of items
     * @param enchant the enchantment
     */
    public void removeEnchantment(ItemStack itemStack, Enchantment enchant) {
        if(!InventoryUtils.isNull(itemStack)) {
            ItemMeta m = itemStack.getItemMeta();
            if(m.hasLore()) {
                List<String> lore = new ArrayList<>();
                for(String l : m.getLore()) {
                    if(!l.startsWith(LORE_PREFIX + enchant.getName() + " ")) {
                        lore.add(l);
                    }
                }
                m.setLore(lore);
                itemStack.setItemMeta(m);
            }
        }
    }
}
