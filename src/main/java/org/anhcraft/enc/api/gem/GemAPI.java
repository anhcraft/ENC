package org.anhcraft.enc.api.gem;

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
 * The Gem API.
 */
public class GemAPI {
    private static final ConcurrentHashMap<String, Gem> GEM_MAP = new ConcurrentHashMap<>();
    
    /**
     * Registers the given gem.
     * @param gem the gem
     */
    public static void registerGem(Gem gem) {
        String id = gem.getId().toUpperCase();
        ExceptionThrower.ifTrue(GEM_MAP.containsKey(id), new Exception("Gem is already registered: Id must be unique"));
        ExceptionThrower.ifFalse(GEM_MAP.values().stream().noneMatch(r ->
                r.getName().equals(gem.getName())), new Exception("Gem is already registered: Name must be unique"));
        GEM_MAP.put(id, gem);
    }

    /**
     * Unregisters the given gem.
     * @param gem the gem
     */
    public static void unregisterGem(Gem gem) {
        String id = gem.getId().toUpperCase();
        ExceptionThrower.ifFalse(GEM_MAP.containsKey(id), new Exception("Enchantment is not registered yet"));
        GEM_MAP.remove(id);
    }

    /**
     * Checks whether the given gem is registered.
     * @param gem the gem
     * @return true if yes
     */
    public static boolean isGemRegistered(Gem gem) {
        return GEM_MAP.containsValue(gem);
    }

    /**
     * Gets a gem by its id.
     * @param gemId gem's id
     * @return the gem
     */
    public static Gem getGemById(String gemId){
        return GEM_MAP.get(gemId.toUpperCase());
    }

    /**
     * Gets a gem by its name.
     * @param gemName gem's name
     * @return the gem
     */
    public static Gem getGemByName(String gemName){
        return GEM_MAP.values().stream().filter(gem ->
                gem.getName().equals(gemName)).findFirst().orElse(null);
    }

    /**
     * Gets all gems that contains the same given enchantment.
     * @param enchantment the enchantment
     * @return list of matched gems
     */
    public static List<Gem> getGemsByEnchantment(Enchantment enchantment){
        return GEM_MAP.values().stream().filter(gem -> gem.getEnchantment()
                .equals(enchantment)).collect(Collectors.toList());
    }

    /**
     * Gets all registered gems.
     * @return list of gems
     */
    public static List<Gem> getRegisteredGems(){
        return new ArrayList<>(GEM_MAP.values());
    }

    /**
     * Gets ids of all registered gems.
     * @return list of ids
     */
    public static List<String> getRegisteredGemIds(){
        return GEM_MAP.values().stream().map(Gem::getId).collect(Collectors.toList());
    }

    /**
     * Gets names of all registered gems.
     * @return list of names
     */
    public static List<String> getRegisteredGemNames(){
        return GEM_MAP.values().stream().map(Gem::getName).collect(Collectors.toList());
    }

    /**
     * Validates whether the given stack of items is a somehow gem.
     * @param itemStack the stack of items
     * @return true if yes
     */
    public static boolean isGemItem(ItemStack itemStack){
        if(!InventoryUtils.isNull(itemStack)) {
            ItemMeta m = itemStack.getItemMeta();
            if(m.hasLore()) {
                Pattern regex = Pattern.compile(ENC.getGeneralConfig().getString("gem.lore_patterns.full_general_regex"));
                return m.getLore().stream().anyMatch(s ->
                        regex.matcher(ChatUtils.reverseColorCode(s)).matches());
            }
        }
        return false;
    }

    /**
     * Validates whether the given stack of items is a certain gem.
     * @param itemStack the stack of items
     * @param gem the gem
     * @return true if yes
     */
    public static boolean isGemItem(ItemStack itemStack, Gem gem){
        if(!InventoryUtils.isNull(itemStack)) {
            ItemMeta m = itemStack.getItemMeta();
            if(m.hasLore()) {
                Pattern regex = Pattern.compile(ENC.getGeneralConfig().getString("gem.lore_patterns.full_individual_regex").replace("{gem_name}", gem.getName()));
                return m.getLore().stream().anyMatch(s -> regex.matcher(ChatUtils.reverseColorCode(s)).find());
            }
        }
        return false;
    }

    /**
     * Searches for a gem that is contained in the given stack of items.
     * @param itemStack the stack of items
     * @return the gem item
     */
    public static GemItem searchGem(ItemStack itemStack){
        if(!InventoryUtils.isNull(itemStack)) {
            ItemMeta m = itemStack.getItemMeta();
            if(m.hasLore()) {
                Pattern regex1 = Pattern.compile(ENC.getGeneralConfig().getString("gem.lore_patterns.full_general_regex"));
                Pattern regex2 = Pattern.compile(ENC.getGeneralConfig().getString("gem.lore_patterns.name_regex"));
                Pattern regex3 = Pattern.compile(ENC.getGeneralConfig().getString("gem.lore_patterns.success_rate_regex"));
                Pattern regex4 = Pattern.compile(ENC.getGeneralConfig().getString("gem.lore_patterns.protection_rate_regex"));
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
                    return new GemItem(getGemByName(name), sr, pr);
                }
            }
        }
        return null;
    }

    /**
     * Assigns the given stack of items to be a gem.
     * @param itemStack stack of items
     * @param gemItem the gem
     */
    public static void assignGem(ItemStack itemStack, GemItem gemItem) {
        if(!InventoryUtils.isNull(itemStack)) {
            ItemMeta m = itemStack.getItemMeta();
            List<String> lore = Chat.color(ENC.getGeneralConfig().getStringList("gem.lore_patterns.full_raw"))
                .stream()
                .map(s -> s.replace("{gem_name}", gemItem.getGem().getName())
                    .replace("{coloured_gem_name}", Chat.color(gemItem.getGem().getName()))
                    .replace("{enchantment_name}", gemItem.getGem().getEnchantment().getName())
                    .replace("{coloured_enchantment_name}", Chat.color(gemItem.getGem().getEnchantment().getName()))
                    .replace("{level}", Integer.toString(gemItem.getGem().getEnchantmentLevel()))
                    .replace("{roman_level}", RomanNumber.toRoman(gemItem.getGem().getEnchantmentLevel()))
                    .replace("{success_rate}", Double.toString(gemItem.getSuccessRate()))
                    .replace("{protection_rate}", Double.toString(gemItem.getProtectionRate()))
                    .replace("{min_success_rate}", Double.toString(gemItem.getGem().getMinSuccessRate()))
                    .replace("{min_protection_rate}", Double.toString(gemItem.getGem().getMinProtectionRate()))
                    .replace("{max_success_rate}", Double.toString(gemItem.getGem().getMaxSuccessRate()))
                    .replace("{max_protection_rate}", Double.toString(gemItem.getGem().getMaxProtectionRate())))
                .collect(Collectors.toList());
            if(m.hasLore()) {
                Pattern regex1 = Pattern.compile(ENC.getGeneralConfig().getString("gem.lore_patterns.name_regex"));
                Pattern regex2 = Pattern.compile(ENC.getGeneralConfig().getString("gem.lore_patterns.success_rate_regex"));
                Pattern regex3 = Pattern.compile(ENC.getGeneralConfig().getString("gem.lore_patterns.protection_rate_regex"));
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
     * Detaches the gem out of the given stack of items.
     * @param itemStack stack of items
     */
    public static void detachGem(ItemStack itemStack){
        if(!InventoryUtils.isNull(itemStack)) {
            ItemMeta m = itemStack.getItemMeta();
            if(m.hasLore()) {
                Pattern regex1 = Pattern.compile(ENC.getGeneralConfig().getString("gem.lore_patterns.name_regex"));
                Pattern regex2 = Pattern.compile(ENC.getGeneralConfig().getString("gem.lore_patterns.success_rate_regex"));
                Pattern regex3 = Pattern.compile(ENC.getGeneralConfig().getString("gem.lore_patterns.protection_rate_regex"));
                List<String> lore = m.getLore().stream().filter(s -> {
                    s = ChatUtils.reverseColorCode(s);
                    return !regex1.matcher(s).find() && !regex2.matcher(s).find() && !regex3.matcher(s).find();
                }).collect(Collectors.toList());
                m.setLore(lore);
                itemStack.setItemMeta(m);
            }
        }
    }
}
