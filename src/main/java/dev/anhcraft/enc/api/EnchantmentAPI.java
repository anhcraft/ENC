package dev.anhcraft.enc.api;

import dev.anhcraft.craftkit.common.utils.ChatUtil;
import dev.anhcraft.craftkit.utils.ItemUtil;
import dev.anhcraft.enc.ENC;
import dev.anhcraft.enc.utils.FormatUtil;
import dev.anhcraft.enc.utils.RomanNumber;
import dev.anhcraft.jvmkit.utils.Condition;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * The Enchantment API.
 */
public class EnchantmentAPI {
    private static final Map<String, Enchantment> ENCHANT_MAP = new ConcurrentHashMap<>();

    /**
     * Registers the given enchantment.
     * @param enchant the enchantment
     */
    public static void registerEnchantment(@NotNull Enchantment enchant) {
        Condition.argNotNull("enchant", enchant);
        var id = enchant.getId().toUpperCase();
        Condition.check(!ENCHANT_MAP.containsKey(id), "Enchantment is already registered");
        ENCHANT_MAP.put(id, enchant);
        enchant.initConfigFile(new File(ENC.ENCHANTMENT_FOLDER, enchant.getId()+".yml"));
        enchant.onRegistered();
        enchant.reloadConfig();
    }

    /**
     * Unregisters the given enchantment.
     * @param enchant the enchantment
     */
    public static void unregisterEnchantment(@NotNull Enchantment enchant) {
        Condition.argNotNull("enchant", enchant);
        var id = enchant.getId().toUpperCase();
        Condition.check(ENCHANT_MAP.containsKey(id), "Enchantment is not registered");
        ENCHANT_MAP.remove(id);
    }

    /**
     * Checks whether the given enchantment is registered.
     * @param enchant the enchantment
     * @return true if yes
     */
    public static boolean isEnchantmentRegistered(@NotNull Enchantment enchant) {
        Condition.argNotNull("enchant", enchant);
        return ENCHANT_MAP.containsValue(enchant);
    }

    /**
     * Gets the enchantment by its unique id.
     * @param enchantId the enchantment's id
     * @return the enchantment (may be null if it is not found)
     */
    public static Enchantment getEnchantmentById(@NotNull String enchantId) {
        Condition.argNotNull("enchantId", enchantId);
        return ENCHANT_MAP.get(enchantId.toUpperCase());
    }

    /**
     * Gets the enchantment by its name.<br>
     * The name can be coloured or not.
     * @param enchantName the enchantment's name
     * @return the enchantment (may be null if it is not found)
     */
    public static Enchantment getEnchantmentByName(String enchantName) {
        Condition.argNotNull("enchantName", enchantName);
        return ENCHANT_MAP.values().stream().filter(enchantment ->
                enchantment.getName().equals(enchantName)).findFirst().orElse(null);
    }

    /**
     * Gets all registered enchantments.
     * @return an array of enchantments
     */
    public static List<Enchantment> getRegisteredEnchantments() {
        return new ArrayList<>(ENCHANT_MAP.values());
    }

    /**
     * Gets names of all registered enchantments.
     * @return an array of names
     */
    public static List<String> getRegisteredEnchantmentNames() {
        return ENCHANT_MAP.values().stream().map(Enchantment::getName).collect(Collectors.toList());
    }

    /**
     * Gets ids of all registered enchantments.
     * @return an array of ids
     */
    public static List<String> getRegisteredEnchantmentIds() {
        return ENCHANT_MAP.values().stream().map(Enchantment::getId).collect(Collectors.toList());
    }

    /**
     * Gets all available enchantments.
     * @return an array of enchantments
     */
    public static List<Enchantment> getAvailableEnchantments() {
        return ENCHANT_MAP.values().stream().filter(Enchantment::isEnabled).collect(Collectors.toList());
    }

    /**
     * Gets names of all available enchantments.
     * @return an array of names
     */
    public static List<String> getAvailableEnchantmentNames() {
        return ENCHANT_MAP.values().stream().filter(Enchantment::isEnabled).map(Enchantment::getName).collect(Collectors.toList());
    }

    /**
     * Gets ids of all available enchantments.
     * @return an array of ids
     */
    public static List<String> getAvailableEnchantmentIds() {
        return ENCHANT_MAP.values().stream().filter(Enchantment::isEnabled).map(Enchantment::getId).collect(Collectors.toList());
    }

    /**
     * Checks whether the given stack of items contains any enchantment.
     * @param itemStack the stack of items
     * @return true if yes
     */
    public static boolean isEnchanted(@Nullable ItemStack itemStack) {
        if(!ItemUtil.isNull(itemStack)) {
            var m = itemStack.getItemMeta();
            if(m.hasLore()) {
                var regex = Pattern.compile(ENC.getGeneralConfig().getString("enchantment.lore_patterns.full_general_regex"));
                return m.getLore().stream().anyMatch(s -> regex.matcher(FormatUtil.reverseColorCode(s)).matches());
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
    public static boolean isEnchanted(@Nullable ItemStack itemStack, @NotNull Enchantment enchant) {
        Condition.argNotNull("enchant", enchant);
        if(!ItemUtil.isNull(itemStack)) {
            var m = itemStack.getItemMeta();
            if(m.hasLore()) {
                var regex = Pattern.compile(ENC.getGeneralConfig().getString("enchantment.lore_patterns.full_individual_regex").replace("{name}", enchant.getName()));
                return m.getLore().stream().anyMatch(s -> regex.matcher(FormatUtil.reverseColorCode(s)).find());
            }
        }
        return false;
    }

    /**
     * Lists all enchantments of the given stack of item.
     * @param itemStack the stack of items
     * @return a map of enchantments which includes their names and their levels
     */
    @NotNull
    public static Map<Enchantment, Integer> listEnchantments(@Nullable ItemStack itemStack) {
        if(!ItemUtil.isNull(itemStack)) {
            var m = itemStack.getItemMeta();
            if(m.hasLore()) {
                var regex1 = Pattern.compile(ENC.getGeneralConfig().getString("enchantment.lore_patterns.full_general_regex"));
                var regex2 = Pattern.compile(ENC.getGeneralConfig().getString("enchantment.lore_patterns.name_regex"));
                var regex3 = Pattern.compile(ENC.getGeneralConfig().getString("enchantment.lore_patterns.level_regex"));
                Map<Enchantment, Integer> map = new HashMap<>();
                for(String l : m.getLore()) {
                    l = FormatUtil.reverseColorCode(l);
                    if(regex1.matcher(l).matches()) {
                        var nameMatcher = regex2.matcher(l);
                        if(nameMatcher.find()){
                            var lvMatcher = regex3.matcher(l);
                            if(lvMatcher.find()){
                                var enc = getEnchantmentByName(nameMatcher.group());
                                if(enc != null) map.put(enc, RomanNumber.toDecimal(lvMatcher.group()));
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
    public static int getEnchantmentLevel(@Nullable ItemStack itemStack, @NotNull Enchantment enchant) {
        Condition.argNotNull("enchant", enchant);
        if(!ItemUtil.isNull(itemStack)) {
            var m = itemStack.getItemMeta();
            if(m.hasLore()) {
                var regex1 = Pattern.compile(ENC.getGeneralConfig().getString("enchantment.lore_patterns.full_individual_regex").replace("{name}", enchant.getName()));
                var regex2 = Pattern.compile(ENC.getGeneralConfig().getString("enchantment.lore_patterns.level_regex"));
                for(var l : m.getLore()) {
                    l = FormatUtil.reverseColorCode(l);
                    if(regex1.matcher(l).matches()) {
                        var nameMatcher = regex2.matcher(l);
                        if(nameMatcher.find()) return RomanNumber.toDecimal(nameMatcher.group());
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
    public static void addEnchantment(@Nullable ItemStack itemStack, @NotNull Enchantment enchant, int level) {
        Condition.argNotNull("enchant", enchant);
        if(!ItemUtil.isNull(itemStack)) {
            var m = itemStack.getItemMeta();
            List<String> lore = new ArrayList<>();
            lore.add(ChatUtil.formatColorCodes(ENC.getGeneralConfig().getString("enchantment.lore_patterns.full_raw"))
                .replace("{name}", enchant.getName())
                .replace("{coloured_name}", ChatUtil.formatColorCodes(enchant.getName()))
                .replace("{level}", Integer.toString(level))
                .replace("{roman_level}", Objects.requireNonNull(RomanNumber.toRoman(level))));
            if(m.hasLore()) {
                var regex = Pattern.compile(ENC.getGeneralConfig().getString("enchantment.lore_patterns.full_individual_regex").replace("{name}", enchant.getName()));
                lore.addAll(m.getLore().stream().filter(s -> !regex.matcher(FormatUtil.reverseColorCode(s)).find()).collect(Collectors.toList()));
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
    public static void removeEnchantment(@Nullable ItemStack itemStack, @NotNull Enchantment enchant) {
        Condition.argNotNull("enchant", enchant);
        if(!ItemUtil.isNull(itemStack)) {
            var m = itemStack.getItemMeta();
            if(m.hasLore()) {
                var regex = Pattern.compile(ENC.getGeneralConfig().getString("enchantment.lore_patterns.full_individual_regex").replace("{name}", enchant.getName()));
                m.setLore(m.getLore().stream().filter(s -> !regex.matcher(FormatUtil.reverseColorCode(s)).find()).collect(Collectors.toList()));
                itemStack.setItemMeta(m);
            }
        }
    }

    /**
     * Removes all existing enchantment out of the given stack of items.
     * @param itemStack the stack of items
     */
    public static void removeEnchantments(@Nullable ItemStack itemStack) {
        if(!ItemUtil.isNull(itemStack)) {
            var m = itemStack.getItemMeta();
            if(m.hasLore()) {
                var regex = Pattern.compile(ENC.getGeneralConfig().getString("enchantment.lore_patterns.full_general_regex"));
                m.setLore(m.getLore().stream().filter(s -> !regex.matcher(FormatUtil.reverseColorCode(s)).matches()).collect(Collectors.toList()));
                itemStack.setItemMeta(m);
            }
        }
    }
}
