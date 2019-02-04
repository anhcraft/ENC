package org.anhcraft.enc.api;

import org.anhcraft.enc.ENC;
import org.anhcraft.enc.utils.ChatUtils;
import org.anhcraft.enc.utils.RomanNumber;
import org.anhcraft.spaciouslib.utils.Chat;
import org.anhcraft.spaciouslib.utils.ExceptionThrower;
import org.anhcraft.spaciouslib.utils.InitialisationValidator;
import org.anhcraft.spaciouslib.utils.InventoryUtils;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class EnchantmentAPI {
    private static final InitialisationValidator INIT_LOCK = new InitialisationValidator();
    private static final HashMap<String, Enchantment> ENCHANT_MAP = new HashMap<>();
    private File enchantFolder;

    public EnchantmentAPI(File enchantFolder){
        try {
            INIT_LOCK.validate();
        } catch(Exception e) {
            e.printStackTrace();
        }
        this.enchantFolder = enchantFolder;
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
        enchant.initConfig(new File(enchantFolder, enchant.getId()+".yml"));
        enchant.onRegistered();
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
     * The name can be coloured or not.
     * @param enchantName the enchantment's name
     * @return the enchantment (may be null if it is not found)
     */
    public Enchantment getEnchantmentByName(String enchantName) {
        return ENCHANT_MAP.values().stream().filter(enchantment ->
                enchantment.getName().equals(enchantName)).findFirst().orElse(null);
    }

    /**
     * Gets all registered enchantments.
     * @return an array of enchantments
     */
    public List<Enchantment> getRegisteredEnchantments() {
        return new ArrayList<>(ENCHANT_MAP.values());
    }

    /**
     * Gets names of all registered enchantments.
     * @return an array of names
     */
    public List<String> getRegisteredEnchantmentNames() {
        return ENCHANT_MAP.values().stream().map(Enchantment::getName).collect(Collectors.toList());
    }

    /**
     * Gets ids of all registered enchantments.
     * @return an array of ids
     */
    public List<String> getRegisteredEnchantmentIds() {
        return ENCHANT_MAP.values().stream().map(Enchantment::getId).collect(Collectors.toList());
    }

    /**
     * Checks whether the given stack of items contains any enchantment.
     * @param itemStack the stack of items
     * @return true if yes
     */
    public boolean isEnchanted(ItemStack itemStack) {
        if(!InventoryUtils.isNull(itemStack)) {
            ItemMeta m = itemStack.getItemMeta();
            if(m.hasLore()) {
                return m.getLore().stream().anyMatch(s -> ChatUtils.reverseColorCode(s).matches(ENC.getGeneralConfig().getString("enchantment.lore_patterns.full_general_regex")));
            }
        }
        return false;
    }

    /**
     * Checks whether the given stack of items contains an enchantment.
     * @param enchant the enchantment
     * @param itemStack the stack of items
     * @return true if yes
     */
    public boolean isEnchanted(ItemStack itemStack, Enchantment enchant) {
        if(!InventoryUtils.isNull(itemStack)) {
            ItemMeta m = itemStack.getItemMeta();
            if(m.hasLore()) {
                Pattern regex = Pattern.compile(ENC.getGeneralConfig().getString("enchantment.lore_patterns.full_individual_regex").replace("{name}", enchant.getName()));
                return m.getLore().stream().anyMatch(s -> {
                    Matcher matcher = regex.matcher(ChatUtils.reverseColorCode(s));
                    return matcher.find() && matcher.group().equals(enchant.getName());
                });
            }
        }
        return false;
    }

    /**
     * Lists all enchantments of the given stack of item.
     * @param itemStack the stack of items
     * @return a map of enchantments which includes their names and their levels
     */
    public HashMap<Enchantment, Integer> listEnchantments(ItemStack itemStack) {
        if(!InventoryUtils.isNull(itemStack)) {
            ItemMeta m = itemStack.getItemMeta();
            if(m.hasLore()) {
                String regex1 = ENC.getGeneralConfig().getString("enchantment.lore_patterns.full_general_regex");
                Pattern regex2 = Pattern.compile(ENC.getGeneralConfig().getString("enchantment.lore_patterns.name_regex"));
                Pattern regex3 = Pattern.compile(ENC.getGeneralConfig().getString("enchantment.lore_patterns.level_regex"));
                HashMap<Enchantment, Integer> map = new HashMap<>();
                for(String l : m.getLore()) {
                    l = ChatUtils.reverseColorCode(l);
                    if(l.matches(regex1)) {
                        Matcher nameMatcher = regex2.matcher(l);
                        if(nameMatcher.find()){
                            Matcher lvMatcher = regex3.matcher(l);
                            if(lvMatcher.find()){
                                map.put(getEnchantmentByName(nameMatcher.group()),
                                        RomanNumber.toDecimal(lvMatcher.group()));
                            }
                        }
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
                String regex1 = ENC.getGeneralConfig().getString("enchantment.lore_patterns.full_individual_regex").replace("{name}", enchant.getName());
                Pattern regex2 = Pattern.compile(ENC.getGeneralConfig().getString("enchantment.lore_patterns.level_regex"));
                for(String l : m.getLore()) {
                    l = ChatUtils.reverseColorCode(l);
                    if(l.matches(regex1)) {
                        Matcher nameMatcher = regex2.matcher(l);
                        if(nameMatcher.find()) {
                            return RomanNumber.toDecimal(nameMatcher.group());
                        }
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
            lore.add(Chat.color(ENC.getGeneralConfig().getString("enchantment.lore_patterns.full_raw"))
                    .replace("{name}", enchant.getName())
                    .replace("{coloured_name}", Chat.color(enchant.getName()))
                    .replace("{level}", Integer.toString(level))
                    .replace("{roman_level}", RomanNumber.toRoman(level)));
            if(m.hasLore()) {
                Pattern regex = Pattern.compile(ENC.getGeneralConfig().getString("enchantment.lore_patterns.full_individual_regex").replace("{name}", enchant.getName()));
                lore.addAll(m.getLore().stream().filter(s -> !regex.matcher(ChatUtils.reverseColorCode(s)).find()).collect(Collectors.toList()));
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
                Pattern regex = Pattern.compile(ENC.getGeneralConfig().getString("enchantment.lore_patterns.full_individual_regex").replace("{name}", enchant.getName()));
                m.setLore(m.getLore().stream().filter(s -> !regex.matcher(ChatUtils.reverseColorCode(s)).find()).collect(Collectors.toList()));
                itemStack.setItemMeta(m);
            }
        }
    }

    /**
     * Removes all existing enchantment out of the given stack of items.
     * @param itemStack the stack of items
     */
    public void removeEnchantments(ItemStack itemStack) {
        if(!InventoryUtils.isNull(itemStack)) {
            ItemMeta m = itemStack.getItemMeta();
            if(m.hasLore()) {
                m.setLore(m.getLore().stream().filter(s -> !ChatUtils.reverseColorCode(s).matches(ENC.getGeneralConfig().getString("enchantment.lore_patterns.full_general_regex"))).collect(Collectors.toList()));
                itemStack.setItemMeta(m);
            }
        }
    }
}
