package dev.anhcraft.enc.api;

import dev.anhcraft.jvmkit.utils.Condition;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents a report of an item stack.
 */
public class ItemReport {
    private final Map<Enchantment, Integer> enchantmentMap = new HashMap<>();
    private Player player;
    private ItemStack itemStack;

    /**
     * Constructs an instance of {@code ItemReport}.
     * @param player the player who used the item
     * @param itemStack the item stack
     * @param enchantmentMap all enchantments on that item stack
     */
    public ItemReport(@NotNull Player player, @NotNull ItemStack itemStack, @Nullable Map<Enchantment, Integer> enchantmentMap) {
        Condition.argNotNull("player", player);
        Condition.argNotNull("itemStack", itemStack);
        this.player = player;
        this.itemStack = itemStack;
        if(enchantmentMap != null) this.enchantmentMap.putAll(enchantmentMap);
    }

    /**
     * Returns the player.
     * @return player
     */
    @NotNull
    public Player getPlayer() {
        return player;
    }

    /**
     * Returns the item stack.
     * @return item stack
     */
    @NotNull
    public ItemStack getItem() {
        return itemStack;
    }

    /**
     * Returns all enchantments on the item stack.
     * @return a map of enchantments which includes their names and their levels
     */
    @NotNull
    public Map<Enchantment, Integer> getEnchantmentMap() {
        return enchantmentMap;
    }
}
