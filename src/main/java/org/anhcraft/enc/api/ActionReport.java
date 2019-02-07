package org.anhcraft.enc.api;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;

/**
 * Represents an action report.<br>
 * The report is sent to an event listener to provide information about previous action.
 */
public class ActionReport {
    private Player player;
    private ItemStack itemStack;
    private HashMap<Enchantment, Integer> enchantmentMap;
    private boolean prevented;

    /**
     * Creates an instance of ActionReport.
     * @param player the player who did the action
     * @param itemStack the stack of items which is checked
     * @param enchantmentMap all available enchantments in the checked stack
     * @param prevented whether the action is going to be prevented
     */
    public ActionReport(Player player, ItemStack itemStack, HashMap<Enchantment, Integer> enchantmentMap, boolean prevented) {
        this.player = player;
        this.itemStack = itemStack;
        this.enchantmentMap = enchantmentMap;
        this.prevented = prevented;
    }

    /**
     * Gets the player who did the action.
     * @return player
     */
    public Player getPlayer() {
        return player;
    }

    /**
     * Gets the stack of items which is checked.
     * @return checked stacks of items
     */
    public ItemStack getItemStack() {
        return itemStack;
    }

    /**
     * Gets all available enchantments in the checked stack.
     * @return a map of available enchantments which includes their names and their levels
     */
    public HashMap<Enchantment, Integer> getEnchantmentMap() {
        return enchantmentMap;
    }

    /**
     * Whether the action is going to be prevented.
     * @return true if yes
     */
    public boolean isPrevented() {
        return prevented;
    }

    /**
     * Overrides the prevent status.<br>
     * Although, not all actions can be prevented by using this method.
     * @param prevented prevent status
     */
    public void setPrevented(boolean prevented) {
        this.prevented = prevented;
    }
}
