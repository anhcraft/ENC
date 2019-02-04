package org.anhcraft.enc.api;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;

/**
 * Represents an action report. The report is sent into an event listener after it has been through a validation.
 */
public class ActionReport {
    private Player player;
    private ItemStack itemStack;
    private HashMap<Enchantment, Integer> enchantmentMap;

    /**
     * Creates an instance of ActionReport.
     * @param player the player who did the action
     * @param itemStack the stack of items which is checked
     * @param enchantmentMap all available enchantments in the checked stack
     */
    public ActionReport(Player player, ItemStack itemStack, HashMap<Enchantment, Integer> enchantmentMap) {
        this.player = player;
        this.itemStack = itemStack;
        this.enchantmentMap = enchantmentMap;
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
}
