package org.anhcraft.enc.api;

import org.anhcraft.algorithmlib.array.searching.ArrayBinarySearch;
import org.anhcraft.enc.utils.ChatUtils;
import org.anhcraft.spaciouslib.builders.EqualsBuilder;
import org.anhcraft.spaciouslib.builders.HashCodeBuilder;
import org.anhcraft.spaciouslib.utils.Chat;
import org.anhcraft.spaciouslib.utils.ExceptionThrower;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;

/**
 * Represents an enchantment.
 */
public abstract class Enchantment {
    private String id;
    private String[] description;
    private String author;
    private ConfigurationSection configuration;
    private String proposer;
    private int maxLevel;
    private EnchantmentTarget[] itemTarget;
    private Chat chat;

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
        ExceptionThrower.ifFalse(id.matches("^[\\w]+$"), new Exception("enchantment id must only contain A-Z, 0-9 and underscore"));
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

    void initConfig(ConfigurationSection config){
        configuration = config;
        chat = new Chat(replaceStr(config.getString("chat_prefix")));
        ExceptionThrower.ifFalse(getName().indexOf(ChatUtils.SECTION_SIGN) == -1, new Exception("enchantment name can not contain section signs due to unexpected bugs, please use ampersands instead"));
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
     * Gets the separate configuration of this enchantment.<br>
     * For applying changes, saves the configuration by calling the method {@link EnchantmentAPI#saveEnchantmentConfig()}.
     * @return enchantment's configuration
     */
    public ConfigurationSection getConfiguration() {
        return configuration;
    }

    /**
     * Gets the name of this enchantment.
     * @return enchantment's name
     */
    public String getName() {
        return configuration.getString("name");
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
        return configuration.getBoolean("enabled");
    }

    /**
     * Checks whether the given world is a allowed place for executing this enchantment.
     * @param world world's name
     * @return true if yes
     */
    public boolean isAllowedWorld(String world) {
        if(configuration.getBoolean("allowed_worlds_list")){
            return configuration.getStringList("worlds_list").contains(world);
        } else {
            return !configuration.getStringList("worlds_list").contains(world);
        }
    }

    /**
     * Gets the separate chat of this enchantment.
     * @return chat
     */
    public Chat getChat() {
        return chat;
    }

    /**
     * Checks whether this enchantment is suitable for the given stack of item
     * @param itemStack the stack of items
     * @return true if yes
     */
    public abstract boolean canEnchantItem(ItemStack itemStack);

    @Override
    public boolean equals(Object object){
        if(object != null && object.getClass() == this.getClass()){
            Enchantment e = (Enchantment) object;
            return new EqualsBuilder()
                    .append(e.id, this.id)
                    .append(e.configuration.getString("name"), this.configuration.getString("name"))
                    .build();
        }
        return false;
    }

    @Override
    public int hashCode(){
        return new HashCodeBuilder(17, 21)
                .append(this.id)
                .append(this.configuration.getString("name"))
                .append(this.author)
                .append(this.itemTarget)
                .append(this.proposer)
                .append(this.maxLevel)
                .append(this.description).build();
    }
}
