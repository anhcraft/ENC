package org.anhcraft.enc.api.rune;

import org.anhcraft.enc.ENC;
import org.anhcraft.enc.api.Enchantment;
import org.anhcraft.enc.utils.ChatUtils;
import org.anhcraft.enc.utils.RomanNumber;
import org.anhcraft.spaciouslib.utils.Chat;
import org.anhcraft.spaciouslib.utils.ExceptionThrower;
import org.anhcraft.spaciouslib.utils.InventoryUtils;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * The Rune API.
 */
public class RuneAPI {
    private static final ConcurrentHashMap<String, Rune> RUNE_MAP = new ConcurrentHashMap<>();
    
    /**
     * Registers the given rune.
     * @param rune the rune
     */
    public static void registerRune(Rune rune) {
        String id = rune.getId().toUpperCase();
        ExceptionThrower.ifTrue(RUNE_MAP.containsKey(id), new Exception("Rune is already registered: Id must be unique"));
        ExceptionThrower.ifFalse(RUNE_MAP.values().stream().noneMatch(r ->
                r.getName().equals(rune.getName())), new Exception("Rune is already registered: Name must be unique"));
        RUNE_MAP.put(id, rune);
    }

    /**
     * Unregisters the given rune.
     * @param rune the rune
     */
    public static void unregisterRune(Rune rune) {
        String id = rune.getId().toUpperCase();
        ExceptionThrower.ifFalse(RUNE_MAP.containsKey(id), new Exception("Enchantment is not registered yet"));
        RUNE_MAP.remove(id);
    }

    /**
     * Checks whether the given rune is registered.
     * @param rune the rune
     * @return true if yes
     */
    public static boolean isRuneRegistered(Rune rune) {
        return RUNE_MAP.containsValue(rune);
    }

    /**
     * Gets a rune by its id.
     * @param runeId rune's id
     * @return the rune
     */
    public static Rune getRuneById(String runeId){
        return RUNE_MAP.get(runeId.toUpperCase());
    }

    /**
     * Gets a rune by its name.
     * @param runeName rune's name
     * @return the rune
     */
    public static Rune getRuneByName(String runeName){
        return RUNE_MAP.values().stream().filter(rune ->
                rune.getName().equals(runeName)).findFirst().orElse(null);
    }

    /**
     * Gets all runes that contains the same given enchantment.
     * @param enchantment the enchantment
     * @return list of matched runes
     */
    public static List<Rune> getRunesByEnchantment(Enchantment enchantment){
        return RUNE_MAP.values().stream().filter(rune -> rune.getEnchantment()
                .equals(enchantment)).collect(Collectors.toList());
    }

    /**
     * Gets all registered runes.
     * @return list of runes
     */
    public static List<Rune> getRegisteredRunes(){
        return new ArrayList<>(RUNE_MAP.values());
    }

    /**
     * Gets ids of all registered runes.
     * @return list of ids
     */
    public static List<String> getRegisteredRuneIds(){
        return RUNE_MAP.values().stream().map(Rune::getId).collect(Collectors.toList());
    }

    /**
     * Gets names of all registered runes.
     * @return list of names
     */
    public static List<String> getRegisteredRuneNames(){
        return RUNE_MAP.values().stream().map(Rune::getName).collect(Collectors.toList());
    }

    /**
     * Validates whether the given stack of items may be a rune.
     * @param itemStack the stack of items
     * @return true if yes
     */
    public static boolean isRuneItem(ItemStack itemStack){
        if(!InventoryUtils.isNull(itemStack)) {
            ItemMeta m = itemStack.getItemMeta();
            if(m.hasLore()) {
                Pattern regex = Pattern.compile(ENC.getGeneralConfig().getString("rune.lore_patterns.full_general_regex"));
                return m.getLore().stream().anyMatch(s ->
                        regex.matcher(ChatUtils.reverseColorCode(s)).matches());
            }
        }
        return false;
    }

    /**
     * Validates whether the given stack of items is a certain rune.
     * @param itemStack the stack of items
     * @param rune the rune
     * @return true if yes
     */
    public static boolean isRuneItem(ItemStack itemStack, Rune rune){
        if(!InventoryUtils.isNull(itemStack)) {
            ItemMeta m = itemStack.getItemMeta();
            if(m.hasLore()) {
                Pattern regex = Pattern.compile(ENC.getGeneralConfig().getString("rune.lore_patterns.full_individual_regex").replace("{rune_name}", rune.getName()));
                return m.getLore().stream().anyMatch(s -> regex.matcher(ChatUtils.reverseColorCode(s)).find());
            }
        }
        return false;
    }

    /**
     * Searches for a rune that is contained in the given stack of items.
     * @param itemStack the stack of items
     * @return the rune item
     */
    public static RuneItem searchRune(ItemStack itemStack){
        if(!InventoryUtils.isNull(itemStack)) {
            ItemMeta m = itemStack.getItemMeta();
            if(m.hasLore()) {
                Pattern regex1 = Pattern.compile(ENC.getGeneralConfig().getString("rune.lore_patterns.full_general_regex"));
                Pattern regex2 = Pattern.compile(ENC.getGeneralConfig().getString("rune.lore_patterns.name_regex"));
                Pattern regex3 = Pattern.compile(ENC.getGeneralConfig().getString("rune.lore_patterns.success_rate_regex"));
                Pattern regex4 = Pattern.compile(ENC.getGeneralConfig().getString("rune.lore_patterns.protection_rate_regex"));
                boolean b = false;
                String name = null;
                double sr = 0, pr = 0;
                for(String l : m.getLore()) {
                    l = ChatUtils.reverseColorCode(l);
                    if(regex1.matcher(l).matches()) {
                        b = true;
                    }
                    Matcher nameMatcher = regex2.matcher(l);
                    if(nameMatcher.find()) {
                        name = nameMatcher.group();
                    }
                    Matcher srMatcher = regex3.matcher(l);
                    if(srMatcher.find()){
                        sr = Double.parseDouble(srMatcher.group());
                    }
                    Matcher prMatcher = regex4.matcher(l);
                    if(prMatcher.find()){
                        pr = Double.parseDouble(prMatcher.group());
                    }
                }
                if(b){
                    return new RuneItem(getRuneByName(name), sr, pr);
                }
            }
        }
        return null;
    }

    /**
     * Assigns the given rune to a stack of items.
     * @param itemStack stack of items
     * @param runeItem the rune item
     */
    public static void assignRune(ItemStack itemStack, RuneItem runeItem) {
        if(!InventoryUtils.isNull(itemStack)) {
            ItemMeta m = itemStack.getItemMeta();
            List<String> lore = Chat.color(ENC.getGeneralConfig().getStringList("rune.lore_patterns.full_raw"))
                .stream()
                .map(s -> s.replace("{rune_name}", runeItem.getRune().getName())
                    .replace("{coloured_rune_name}", Chat.color(runeItem.getRune().getName()))
                    .replace("{enchantment_name}", runeItem.getRune().getEnchantment().getName())
                    .replace("{coloured_enchantment_name}", Chat.color(runeItem.getRune().getEnchantment().getName()))
                    .replace("{level}", Integer.toString(runeItem.getRune().getEnchantmentLevel()))
                    .replace("{roman_level}", RomanNumber.toRoman(runeItem.getRune().getEnchantmentLevel()))
                    .replace("{success_rate}", Double.toString(runeItem.getSuccessRate()))
                    .replace("{protection_rate}", Double.toString(runeItem.getProtectionRate()))
                    .replace("{min_success_rate}", Double.toString(runeItem.getRune().getMinSuccessRate()))
                    .replace("{min_protection_rate}", Double.toString(runeItem.getRune().getMinProtectionRate()))
                    .replace("{max_success_rate}", Double.toString(runeItem.getRune().getMaxSuccessRate()))
                    .replace("{max_protection_rate}", Double.toString(runeItem.getRune().getMaxProtectionRate())))
                .collect(Collectors.toList());
            if(m.hasLore()) {
                Pattern regex1 = Pattern.compile(ENC.getGeneralConfig().getString("rune.lore_patterns.name_regex"));
                Pattern regex2 = Pattern.compile(ENC.getGeneralConfig().getString("rune.lore_patterns.success_rate_regex"));
                Pattern regex3 = Pattern.compile(ENC.getGeneralConfig().getString("rune.lore_patterns.protection_rate_regex"));
                lore.addAll(m.getLore().stream().filter(s -> {
                    s = ChatUtils.reverseColorCode(s);
                    return !regex1.matcher(s).find() && !regex2.matcher(s).find() && !regex3.matcher(s).find();
                }).collect(Collectors.toList()));
            }
            m.setLore(lore);
            itemStack.setItemMeta(m);
        }
    }

    /**
     * Detaches the rune from the given stack of items.
     * @param itemStack stack of items
     */
    public static void detachRune(ItemStack itemStack){
        if(!InventoryUtils.isNull(itemStack)) {
            ItemMeta m = itemStack.getItemMeta();
            if(m.hasLore()) {
                Pattern regex1 = Pattern.compile(ENC.getGeneralConfig().getString("rune.lore_patterns.name_regex"));
                Pattern regex2 = Pattern.compile(ENC.getGeneralConfig().getString("rune.lore_patterns.success_rate_regex"));
                Pattern regex3 = Pattern.compile(ENC.getGeneralConfig().getString("rune.lore_patterns.protection_rate_regex"));
                List<String> lore = m.getLore().stream().filter(s -> {
                    s = ChatUtils.reverseColorCode(s);
                    return !regex1.matcher(s).find() && !regex2.matcher(s).find() && !regex3.matcher(s).find();
                }).collect(Collectors.toList());
                m.setLore(lore);
                itemStack.setItemMeta(m);
            }
        }
    }

    /**
     * Tries to apply the given rune item
     * @param rune the rune item
     * @return the final result
     */
    public static Rune.ApplyResult tryApplyRune(RuneItem rune) {
        if(Math.random() <= rune.getSuccessRate()/100d){
            return Rune.ApplyResult.SUCCESS;
        } else {
            if(Math.random() <= rune.getProtectionRate()/100d){
                return Rune.ApplyResult.FAILURE;
            } else {
                return Rune.ApplyResult.BROKEN;
            }
        }
    }
}
