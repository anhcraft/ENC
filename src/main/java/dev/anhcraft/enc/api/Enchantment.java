package dev.anhcraft.enc.api;

import dev.anhcraft.craftkit.chat.Chat;
import dev.anhcraft.enc.ENC;
import dev.anhcraft.enc.api.listeners.IListener;
import dev.anhcraft.enc.utils.*;
import dev.anhcraft.jvmkit.lang.annotation.Beta;
import dev.anhcraft.jvmkit.utils.ArrayUtil;
import dev.anhcraft.jvmkit.utils.Condition;
import dev.anhcraft.jvmkit.utils.MathUtil;
import kotlin.Pair;
import net.objecthunter.exp4j.ExpressionBuilder;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Represents an enchantment.
 */
public abstract class Enchantment {
    private static final ExpressionParser EXPRESSION_PARSER = ExpressionParser.valueOf(ENC.getGeneralConfig().getString("plugin.expression_parser").toUpperCase());
    private static final List<String> DEFAULT_WORLDS_LIST = ArrayUtil.toList(new String[]{"$all"});

    private String id;
    private String[] description;
    private String author;
    private String proposer;
    private int maxLevel;
    private EnchantmentTarget[] itemTargets;
    private File configFile;
    private Chat chat;
    private YamlConfiguration config;
    private final List<IListener> eventListeners = new ArrayList<>();
    private final List<String> worldList = new ArrayList<>();
    private final Map<String, Pair<Double, Long>> computation_caching = new HashMap<>();

    /**
     * Creates an instance of enchantment.
     * @param id the enchantment id (A-Z, 0-9 and underscore only, ignored case sensitive)
     * @param description the description
     * @param author the author
     * @param proposer the proposer
     * @param maxLevel the maximum level that a player can enchant up to
     * @param targets item types that may fit the enchantment
     */
    public Enchantment(@NotNull String id, @Nullable String[] description, @NotNull String author, @Nullable String proposer, int maxLevel, @NotNull EnchantmentTarget... targets) {
        Condition.argNotNull("id", id);
        Condition.argNotNull("author", author);
        Condition.check(id.matches("^[\\w]+$"), "enchantment id must not empty and can only contain A-Z, 0-9 and underscore");
        this.id = id;
        this.description = description == null ? new String[0] : Arrays.copyOf(description, description.length);
        this.author = author;
        this.proposer = proposer;
        this.maxLevel = Math.max(maxLevel, 1);
        this.itemTargets = ArrayUtil.binarySearch(targets, EnchantmentTarget.ALL) >= 0 ? new EnchantmentTarget[]{EnchantmentTarget.ALL} : targets; // optimize the targets
    }

    private String replaceStr(String str){
        return str.replace("{lowercase_enchant_id}", id.toLowerCase())
                .replace("{uppercase_enchant_id}", id.toUpperCase())
                .replace("{enchant_id}", id);
    }

    /**
     * Initializes given configuration entries.<br>
     * A configuration entry is only set in case of absent. Otherwise the current value is still be kept.<br>
     * File saving is automatic if there is at least one new entry.
     */
    public <V> void initConfigEntries(@NotNull Map<String, V> map){
        Condition.argNotNull("map", map);
        int i = 0;
        for(Map.Entry<String, V> entry : map.entrySet()) {
            if(!config.isSet(entry.getKey())) {
                Condition.check(!entry.getValue().getClass().isArray(),
                        "Do not use array, switch to List instead");
                config.set(entry.getKey(), entry.getValue());
                i++;
            }
        }
        if(i > 0) saveConfig();
    }

    void initConfigFile(File configFile){
        this.configFile = configFile;
        try {
            configFile.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Get the unique id of this enchantment.
     * @return id of this enchantment
     */
    public String getId() {
        return id;
    }

    /**
     * Gets the list of strings which describes about this enchantment.
     * @return enchantment's description
     */
    public String[] getDescription() {
        return description;
    }

    /**
     * Gets the author who made this enchantment.
     * @return enchantment's author
     */
    public String getAuthor() {
        return author;
    }

    /**
     * Gets the configuration of this enchantment.<br>
     * If you are trying to set new entries, it is recommended to use the method {@link Enchantment#initConfigEntries(Map)}.
     * @return enchantment's configuration
     */
    @NotNull
    public ConfigurationSection getConfig() {
        return config;
    }

    /**
     * Computes the value of a configuration entry by using the information from the given {@link ItemReport}.
     * @param key the key of the entry
     * @param report the report of the item
     * @return the computed value
     */
    public double computeConfigValue(@NotNull String key, @NotNull ItemReport report) {
        Condition.argNotNull("key", key);
        Condition.argNotNull("report", report);
        if(config.getBoolean("general.computation_caching.enabled")){
            Pair<Double, Long> ent = computation_caching.get(key);
            if(ent != null){
                if(System.currentTimeMillis()-ent.getSecond() <= config.getLong("general.computation_caching.caching_time"))
                    return ent.getFirst();
            }
        }
        double v = -1.0;
        Object value = config.get(key);
        boolean ignored = false;
        if(value instanceof String){
            String str = (String) value;
            Pattern levelCheck = Pattern.compile(ENC.getGeneralConfig().getString("enchantment.config_value_computing.placeholder_patterns.level.full_regex"));
            Pattern levelGet = Pattern.compile(ENC.getGeneralConfig().getString("enchantment.config_value_computing.placeholder_patterns.level.value_regex"));
            Matcher levelCheck_;
            // find all {level} placeholder
            while((levelCheck_ = levelCheck.matcher(str)).find()){
                // when get a {level} placeholder, check its is {level} or {level:<ENCHANTMENT_ID>}
                Matcher levelGet_ = levelGet.matcher(levelCheck_.group());
                // if {level:<ENCHANTMENT_ID>}
                if(levelGet_.find()) str = levelCheck_.replaceFirst(Integer.toString(report.getEnchantmentMap()
                            .get(EnchantmentAPI.getEnchantmentById(levelGet_.group()))));
                else str = levelCheck_.replaceFirst(Integer.toString(report.getEnchantmentMap().get(this))); // or {level}
                ignored = true;
            }

            Pattern maxLevelCheck = Pattern.compile(ENC.getGeneralConfig().getString("enchantment.config_value_computing.placeholder_patterns.max_level.full_regex"));
            Pattern maxLevelGet = Pattern.compile(ENC.getGeneralConfig().getString("enchantment.config_value_computing.placeholder_patterns.max_level.value_regex"));
            Matcher maxLevelCheck_;
            // find all {max_level} placeholder
            while((maxLevelCheck_ = maxLevelCheck.matcher(str)).find()){
                // when get a {max_level} placeholder, check its is {max_level} or {max_level:<ENCHANTMENT_ID>}
                Matcher maxLevelGet_ = maxLevelGet.matcher(maxLevelCheck_.group());
                // if {max_level:<ENCHANTMENT_ID>}
                if(maxLevelGet_.find()) str = maxLevelCheck_.replaceFirst(Integer.toString(EnchantmentAPI.getEnchantmentById(maxLevelGet_.group()).maxLevel));
                else str = maxLevelCheck_.replaceFirst(Integer.toString(maxLevel)); // or {max_level}
            }

            switch(EXPRESSION_PARSER){
                case JAVASCRIPT:
                    v = ScriptUtil.eval(str);
                    break;
                case EXP4J:
                    v = new ExpressionBuilder(str).build().evaluate();
                    break;
            }
        }
        if(config.getBoolean("general.computation_caching.enabled") &&
                (!config.getBoolean("general.computation_caching.strict_mode") || !ignored))
            computation_caching.put(key, new Pair<>(v, System.currentTimeMillis()));
        return v;
    }

    /**
     * Reloads the configuration of this enchantment.
     */
    public void reloadConfig() {
        config = YamlConfiguration.loadConfiguration(configFile);

        Map<String, Object> map = new HashMap<>();
        map.put("general.enabled", true);
        map.put("general.chat_prefix", "&5#{lowercase_enchant_id} > &f");
        map.put("general.worlds_list", new ArrayList<>(DEFAULT_WORLDS_LIST));
        map.put("general.allowed_worlds_list", true);
        map.put("general.name", id);
        map.put("general.computation_caching.enabled", false);
        map.put("general.computation_caching.caching_time", 72000);
        map.put("general.computation_caching.strict_mode", true);
        initConfigEntries(map);

        chat = new Chat(replaceStr(config.getString("general.chat_prefix")));
        Condition.check(getName().indexOf(FormatUtil.SECTION_SIGN) == -1, "enchantment name can not contain section signs due to unexpected bugs, please use ampersands instead");
        worldList.clear();
        Map<String, List<String>> groups = new HashMap<>();
        groups.put("all", Bukkit.getWorlds().stream().map(World::getName).collect(Collectors.toList()));
        worldList.addAll(ReplaceUtil.replaceVariables(config.getStringList("general.worlds_list"), groups, true));
        onInitConfig();
    }

    /**
     * Saves the configuration of this enchantment.
     */
    public synchronized void saveConfig() {
        try {
            config.save(configFile);
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Gets the name of this enchantment.
     * @return enchantment's name
     */
    public String getName() {
        return config.getString("general.name");
    }

    /**
     * Returns types of item that may fit this enchantment.
     * @return enchantment's target item types
     */
    @NotNull
    public EnchantmentTarget[] getItemTargets() {
        return itemTargets;
    }

    /**
     * Gets the maximum level that a player can enchant up to.
     * @return enchantment's maximum level
     */
    public int getMaxLevel() {
        return maxLevel;
    }

    /**
     * Gets the name of the proposer.
     * @return enchantment's proposer (maybe null if there is not a proposer)
     */
    @Nullable
    public String getProposer() {
        return proposer;
    }

    /**
     * Checks whether this enchantment is enabled or not.
     * @return true if yes
     */
    public boolean isEnabled() {
        return config.getBoolean("general.enabled");
    }

    /**
     * Checks whether the given world is a allowed place for executing this enchantment.
     * @param world world's name
     * @return true if yes
     */
    public boolean isAllowedWorld(@Nullable String world) {
        return world != null && config.getBoolean("general.allowed_worlds_list") == worldList.contains(world);
    }

    /**
     * Gets the separate chat of this enchantment.
     * @return chat
     */
    public Chat getChat() {
        return chat;
    }

    /**
     * Gets the list of event listeners.<br>
     * The list is mutable which means can be modified.
     * @return list of event listeners
     */
    public List<IListener> getEventListeners() {
        return eventListeners;
    }

    /**
     * Checks whether this enchantment is suitable for the given stack of item.
     * @param itemStack the stack of items
     * @return true if yes
     */
    public boolean canEnchantItem(@Nullable ItemStack itemStack){
        if(itemStack != null) {
            for (EnchantmentTarget t : getItemTargets()) {
                if (t.includes(itemStack)) return true;
            }
        }
        return false;
    }

    /**
     * This method is called after this enchantment is registered successfully.<br>
     * Do not use this method to register custom configuration entries, use {@link #onInitConfig()} instead.
     */
    public void onRegistered(){}

    /**
     * This method is called whenever the configuration is reloaded.
     */
    public void onInitConfig(){}

    @Beta
    protected boolean handleCooldown(Map<Player, Cooldown> map, Player player, double cooldown){
        Cooldown cooldownTimer = map.get(player);
        if(cooldownTimer == null) map.put(player, new Cooldown());
        else {
            if(cooldownTimer.isTimeout(cooldown)) cooldownTimer.reset();
            else {
                getChat().message(player, "Remaining cooldown time: "+ MathUtil.round(UnitUtil.tick2s(cooldown-cooldownTimer.elapsedTicks()), 2) +"s");
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Enchantment that = (Enchantment) o;
        return maxLevel == that.maxLevel &&
                id.equals(that.id) &&
                Arrays.equals(description, that.description) &&
                Objects.equals(author, that.author) &&
                Objects.equals(proposer, that.proposer) &&
                Arrays.equals(itemTargets, that.itemTargets) &&
                Objects.equals(configFile, that.configFile) &&
                Objects.equals(chat, that.chat) &&
                config.equals(that.config) &&
                eventListeners.equals(that.eventListeners) &&
                worldList.equals(that.worldList) &&
                computation_caching.equals(that.computation_caching);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
