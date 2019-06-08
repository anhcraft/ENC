package dev.anhcraft.enc.api;

import dev.anhcraft.jvmkit.utils.Condition;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.UUID;

/**
 * Represents an action report.<br>
 * The report is sent to an event listener to provide information about previous action.
 */
public class ActionReport {
    private UUID player;
    private ItemStack itemStack;
    private Map<Enchantment, Integer> enchantmentMap;
    private boolean prevented;

    /**
     * Creates an instance of ActionReport.
     * @param player the player who did the action
     * @param itemStack the stack of items which is validated
     * @param enchantmentMap all available enchantments in the checked stack
     * @param prevented whether the action is going to be prevented
     */
    public ActionReport(@NotNull Player player, @NotNull ItemStack itemStack, @NotNull  Map<Enchantment, Integer> enchantmentMap, boolean prevented) {
        Condition.argNotNull("player", player);
        Condition.argNotNull("itemStack", itemStack);
        Condition.argNotNull("enchantmentMap", enchantmentMap);
        this.player = player.getUniqueId();
        this.itemStack = itemStack;
        this.enchantmentMap = enchantmentMap;
        this.prevented = prevented;
    }

    /**
     * Gets the player who did the action.
     * @return player
     */
    @NotNull
    public Player getPlayer() {
        return Bukkit.getPlayer(player);
    }

    /**
     * Gets the stack of items which is validated.
     * @return validated stack of items
     */
    public ItemStack getItemStack() {
        return itemStack;
    }

    /**
     * Gets all available enchantments in the checked stack.
     * @return a map of available enchantments which includes their names and their levels
     */
    public Map<Enchantment, Integer> getEnchantmentMap() {
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
