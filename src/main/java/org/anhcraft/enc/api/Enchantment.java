package org.anhcraft.enc.api;

import org.anhcraft.algorithmlib.array.searching.ArrayBinarySearch;
import org.anhcraft.enc.ENC;
import org.anhcraft.enc.api.listeners.IListener;
import org.anhcraft.enc.utils.ChatUtils;
import org.anhcraft.enc.utils.ReplaceUtils;
import org.anhcraft.spaciouslib.builders.EqualsBuilder;
import org.anhcraft.spaciouslib.builders.HashCodeBuilder;
import org.anhcraft.spaciouslib.io.FileManager;
import org.anhcraft.spaciouslib.utils.Chat;
import org.anhcraft.spaciouslib.utils.CommonUtils;
import org.anhcraft.spaciouslib.utils.ExceptionThrower;
import org.anhcraft.spaciouslib.utils.MathUtils;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.inventory.ItemStack;

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
    private static final List<String> DEFAULT_WORLDS_LIST = CommonUtils.toList(new String[]{
            "$all",
            "-world"
    });

    private String id;
    private String[] description;
    private String author;
    private String proposer;
    private int maxLevel;
    private EnchantmentTarget[] itemTarget;
    private final YamlConfiguration config = new YamlConfiguration();
    private File configFile;
    private Chat chat;
    private final List<IListener> eventListeners = new ArrayList<>();
    private final List<String> worldList = new ArrayList<>();

    /**
     * Creates an instance of enchantment.
     * @param id the enchantment id (A-Z, 0-9 and underscore only, ignored case sensitive)
     * @param description the description
     * @param author the author
     * @param proposer the proposer (can be null)
     * @param maxLevel the maximum level that a player can enchant up to
     * @param targets item types that may fit the enchantment
     */
    public Enchantment(String id, String[] description, String author, String proposer, int maxLevel, EnchantmentTarget... targets) {
        ExceptionThrower.ifFalse(id.matches("^[\\w]+$"), new Exception("enchantment id must not empty and can only contain A-Z, 0-9 and underscore"));
        this.id = id;
        this.description = Arrays.copyOf(description, description.length);
        this.author = author;
        this.proposer = proposer;
        this.maxLevel = Math.max(maxLevel, 1);
        this.itemTarget = ArrayBinarySearch.search(targets, EnchantmentTarget.ALL) >= 0 ? new EnchantmentTarget[]{EnchantmentTarget.ALL} : targets; // optimize the targets
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
    public <V> void initConfigEntries(Map<String, V> map){
        int i = 0;
        for(Map.Entry<String, V> entry : map.entrySet()) {
            if(!config.isSet(entry.getKey())) {
                config.set(entry.getKey(), entry.getValue());
                i++;
            }
        }
        if(i > 0){
            saveConfig();
        }
    }

    void initConfig(File configFile){
        this.configFile = configFile;
        new FileManager(configFile).create();
        reloadConfig();
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
    public ConfigurationSection getConfig() {
        return config;
    }

    /**
     * Computes the value of a configuration entry by its key.<br>
     * By using the given action report, all placeholder will be replaced.
     * @param key the key
     * @param report the action report
     * @return the computed value
     */
    public double computeConfigValue(String key, ActionReport report) {
        Object value = config.get(key);
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
                if(levelGet_.find()){
                    str = levelCheck_.replaceFirst(Integer.toString(report.getEnchantmentMap()
                            .get(EnchantmentAPI.getEnchantmentById(levelGet_.group()))));
                } else { // or {level}
                    str = levelCheck_.replaceFirst(Integer.toString(report.getEnchantmentMap().get(this)));
                }
            }

            Pattern maxLevelCheck = Pattern.compile(ENC.getGeneralConfig().getString("enchantment.config_value_computing.placeholder_patterns.max_level.full_regex"));
            Pattern maxLevelGet = Pattern.compile(ENC.getGeneralConfig().getString("enchantment.config_value_computing.placeholder_patterns.max_level.value_regex"));
            Matcher maxLevelCheck_;
            // find all {max_level} placeholder
            while((maxLevelCheck_ = maxLevelCheck.matcher(str)).find()){
                // when get a {max_level} placeholder, check its is {max_level} or {max_level:<ENCHANTMENT_ID>}
                Matcher maxLevelGet_ = maxLevelGet.matcher(maxLevelCheck_.group());
                // if {max_level:<ENCHANTMENT_ID>}
                if(maxLevelGet_.find()){
                    str = maxLevelCheck_.replaceFirst(Integer.toString(EnchantmentAPI.getEnchantmentById(maxLevelGet_.group()).maxLevel));
                } else { // or {max_level}
                    str = maxLevelCheck_.replaceFirst(Integer.toString(maxLevel));
                }
            }
            return MathUtils.eval(str);
        } else {
            return -1;
        }
    }

    /**
     * Reloads the configuration of this enchantment.
     */
    public void reloadConfig() {
        try {
            config.load(configFile);
        } catch(IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }
        HashMap<String, Object> map = new HashMap<>();
        map.put("enabled", true);
        map.put("chat_prefix", "&5#{lowercase_enchant_id} > &f");
        map.put("worlds_list", new ArrayList<>(DEFAULT_WORLDS_LIST));
        map.put("allowed_worlds_list", true);
        map.put("name", id);
        initConfigEntries(map);

        chat = new Chat(replaceStr(config.getString("chat_prefix")));
        ExceptionThrower.ifFalse(getName().indexOf(ChatUtils.SECTION_SIGN) == -1, new Exception("enchantment name can not contain section signs due to unexpected bugs, please use ampersands instead"));
        worldList.clear();
        HashMap<String, List<String>> groups = new HashMap<>();
        groups.put("all", Bukkit.getWorlds().stream().map(World::getName).collect(Collectors.toList()));
        worldList.addAll(ReplaceUtils.replaceVariables(config.getStringList("worlds_list"),
                groups, true));
    }

    /**
     * Saves the configuration of this enchantment.
     */
    public void saveConfig() {
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
        return config.getString("name");
    }

    /**
     * Gets types of item that may fit this enchantment.
     * @return enchantment's target item types
     */
    public EnchantmentTarget[] getItemTarget() {
        return itemTarget;
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
    public String getProposer() {
        return proposer;
    }

    /**
     * Checks whether this enchantment is enabled or not.
     * @return true if yes
     */
    public boolean isEnabled() {
        return config.getBoolean("enabled");
    }

    /**
     * Checks whether the given world is a allowed place for executing this enchantment.
     * @param world world's name
     * @return true if yes
     */
    public boolean isAllowedWorld(String world) {
        return config.getBoolean("allowed_worlds_list") == worldList.contains(world);
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
    public abstract boolean canEnchantItem(ItemStack itemStack);

    /**
     * This method is called after this enchantment is registered successfully.
     */
    public void onRegistered(){}

    @Override
    public boolean equals(Object object){
        if(object != null && object.getClass() == this.getClass()){
            Enchantment e = (Enchantment) object;
            return new EqualsBuilder().append(e.id, this.id).build();
        }
        return false;
    }

    @Override
    public int hashCode(){
        return new HashCodeBuilder(17, 21)
                .append(this.id)
                .append(this.author)
                .append(this.itemTarget)
                .append(this.proposer)
                .append(this.maxLevel)
                .append(this.description).build();
    }
}
