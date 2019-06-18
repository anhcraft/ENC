package dev.anhcraft.enc.api.gem;

import dev.anhcraft.craftkit.common.utils.ChatUtil;
import dev.anhcraft.craftkit.utils.ItemUtil;
import dev.anhcraft.enc.ENC;
import dev.anhcraft.enc.api.Enchantment;
import dev.anhcraft.enc.utils.FormatUtil;
import dev.anhcraft.enc.utils.RomanNumber;
import dev.anhcraft.jvmkit.utils.Condition;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * The Gem API.
 */
public class GemAPI {
    private static final Map<String, Gem> GEM_MAP = new ConcurrentHashMap<>();
    
    /**
     * Registers the given gem.
     * @param gem the gem
     */
    public static void registerGem(@NotNull Gem gem) {
        Condition.argNotNull("gem", gem);
        var id = gem.getId().toUpperCase();
        Condition.check(!GEM_MAP.containsKey(id), "Gem is already registered: Id must be unique");
        Condition.check(GEM_MAP.values().stream().noneMatch(r ->
                r.getName().equals(gem.getName())), "Gem is already registered: Name must be unique");
        GEM_MAP.put(id, gem);
    }

    /**
     * Unregisters the given gem.
     * @param gem the gem
     */
    public static void unregisterGem(@NotNull Gem gem) {
        Condition.argNotNull("gem", gem);
        var id = gem.getId().toUpperCase();
        Condition.check(GEM_MAP.containsKey(id), "Enchantment is not registered");
        GEM_MAP.remove(id);
    }

    /**
     * Checks whether the given gem is registered.
     * @param gem the gem
     * @return true if yes
     */
    public static boolean isGemRegistered(@NotNull Gem gem) {
        Condition.argNotNull("gem", gem);
        return GEM_MAP.containsValue(gem);
    }

    /**
     * Gets a gem by its id.
     * @param gemId gem's id
     * @return the gem
     */
    @Nullable
    public static Gem getGemById(@NotNull String gemId){
        Condition.argNotNull("gemId", gemId);
        return GEM_MAP.get(gemId.toUpperCase());
    }

    /**
     * Gets a gem by its name.
     * @param gemName gem's name
     * @return the gem
     */
    @Nullable
    public static Gem getGemByName(@NotNull String gemName){
        Condition.argNotNull("gemName", gemName);
        return GEM_MAP.values().stream().filter(gem ->
                gem.getName().equals(gemName)).findFirst().orElse(null);
    }

    /**
     * Gets all gems that contains the same given enchantment.
     * @param enchantment the enchantment
     * @return list of matched gems
     */
    @NotNull
    public static List<Gem> getGemsByEnchantment(@NotNull Enchantment enchantment){
        Condition.argNotNull("enchantment", enchantment);
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
    public static boolean isGemItem(@Nullable ItemStack itemStack){
        if(!ItemUtil.isNull(itemStack)) {
            var m = itemStack.getItemMeta();
            if(m.hasLore()) {
                var regex = Pattern.compile(ENC.getGeneralConfig().getString("gem.lore_patterns.full_general_regex"));
                return m.getLore().stream().anyMatch(s ->
                        regex.matcher(FormatUtil.reverseColorCode(s)).matches());
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
    public static boolean isGemItem(@Nullable ItemStack itemStack, @NotNull Gem gem){
        if(!ItemUtil.isNull(itemStack)) {
            Condition.argNotNull("gem", gem);
            var m = itemStack.getItemMeta();
            if(m.hasLore()) {
                var regex = Pattern.compile(ENC.getGeneralConfig().getString("gem.lore_patterns.full_individual_regex").replace("{gem_name}", gem.getName()));
                return m.getLore().stream().anyMatch(s -> regex.matcher(FormatUtil.reverseColorCode(s)).find());
            }
        }
        return false;
    }

    /**
     * Searches for a gem that is contained in the given stack of items.
     * @param itemStack the stack of items
     * @return the gem item
     */
    @Nullable
    public static GemItem searchGem(@Nullable ItemStack itemStack){
        if(!ItemUtil.isNull(itemStack)) {
            var m = itemStack.getItemMeta();
            if(m.hasLore()) {
                var regex1 = Pattern.compile(ENC.getGeneralConfig().getString("gem.lore_patterns.full_general_regex"));
                var regex2 = Pattern.compile(ENC.getGeneralConfig().getString("gem.lore_patterns.name_regex"));
                var regex3 = Pattern.compile(ENC.getGeneralConfig().getString("gem.lore_patterns.success_rate_regex"));
                var regex4 = Pattern.compile(ENC.getGeneralConfig().getString("gem.lore_patterns.protection_rate_regex"));
                var b = false;
                String name = null;
                double sr = 0, pr = 0;
                for(var l : m.getLore()) {
                    l = FormatUtil.reverseColorCode(l);
                    if(regex1.matcher(l).matches()) b = true;

                    var nameMatcher = regex2.matcher(l);
                    if(nameMatcher.find()) name = nameMatcher.group();

                    var srMatcher = regex3.matcher(l);
                    if(srMatcher.find())  sr = Double.parseDouble(srMatcher.group());

                    var prMatcher = regex4.matcher(l);
                    if(prMatcher.find()) pr = Double.parseDouble(prMatcher.group());
                }
                if(b && name != null) {
                    var g = getGemByName(name);
                    if(g != null) return new GemItem(g, sr, pr);
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
    public static void assignGem(@Nullable ItemStack itemStack, @NotNull GemItem gemItem) {
        if(!ItemUtil.isNull(itemStack)) {
            Condition.argNotNull("gemItem", gemItem);
            var m = itemStack.getItemMeta();
            var lore = ChatUtil.formatColorCodes(ENC.getGeneralConfig()
                    .getStringList("gem.lore_patterns.full_raw"))
                .stream()
                .map(s -> s.replace("{gem_name}", gemItem.getGem().getName())
                    .replace("{coloured_gem_name}", ChatUtil.formatColorCodes(gemItem.getGem().getName()))
                    .replace("{enchantment_name}", gemItem.getGem().getEnchantment().getName())
                    .replace("{coloured_enchantment_name}", ChatUtil.formatColorCodes(gemItem.getGem().getEnchantment().getName()))
                    .replace("{level}", Integer.toString(gemItem.getGem().getEnchantmentLevel()))
                    .replace("{roman_level}", Objects.requireNonNull(
                            RomanNumber.toRoman(gemItem.getGem().getEnchantmentLevel())))
                    .replace("{success_rate}", Double.toString(gemItem.getSuccessRate()))
                    .replace("{protection_rate}", Double.toString(gemItem.getProtectionRate()))
                    .replace("{min_success_rate}", Double.toString(gemItem.getGem().getMinSuccessRate()))
                    .replace("{min_protection_rate}", Double.toString(gemItem.getGem().getMinProtectionRate()))
                    .replace("{max_success_rate}", Double.toString(gemItem.getGem().getMaxSuccessRate()))
                    .replace("{max_protection_rate}", Double.toString(gemItem.getGem().getMaxProtectionRate())))
                .collect(Collectors.toList());
            if(m.hasLore()) {
                var regex1 = Pattern.compile(ENC.getGeneralConfig().getString("gem.lore_patterns.name_regex"));
                var regex2 = Pattern.compile(ENC.getGeneralConfig().getString("gem.lore_patterns.success_rate_regex"));
                var regex3 = Pattern.compile(ENC.getGeneralConfig().getString("gem.lore_patterns.protection_rate_regex"));
                lore.addAll(m.getLore().stream().filter(s -> {
                    s = FormatUtil.reverseColorCode(s);
                    return !regex1.matcher(s).find() && !regex2.matcher(s).find() && !regex3.matcher(s).find();
                }).collect(Collectors.toList()));
            }
            m.setLore(lore);
            itemStack.setItemMeta(m);
        }
    }

    /**
     * Detaches the gem from the given stack of items.
     * @param itemStack stack of items
     */
    public static void detachGem(@Nullable ItemStack itemStack){
        if(!ItemUtil.isNull(itemStack)) {
            var m = itemStack.getItemMeta();
            if(m.hasLore()) {
                var regex1 = Pattern.compile(ENC.getGeneralConfig().getString("gem.lore_patterns.name_regex"));
                var regex2 = Pattern.compile(ENC.getGeneralConfig().getString("gem.lore_patterns.success_rate_regex"));
                var regex3 = Pattern.compile(ENC.getGeneralConfig().getString("gem.lore_patterns.protection_rate_regex"));
                var lore = m.getLore().stream().filter(s -> {
                    s = FormatUtil.reverseColorCode(s);
                    return !regex1.matcher(s).find() && !regex2.matcher(s).find() && !regex3.matcher(s).find();
                }).collect(Collectors.toList());
                m.setLore(lore);
                itemStack.setItemMeta(m);
            }
        }
    }
}
