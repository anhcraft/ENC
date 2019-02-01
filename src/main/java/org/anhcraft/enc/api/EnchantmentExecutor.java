package org.anhcraft.enc.api;

import org.bukkit.entity.Player;

/**
 * Represents an enchantment executor.
 */
public class EnchantmentExecutor {
    private Player player;
    private Enchantment enchantment;
    private int enchantmentLevel;

    /**
     * Creates an instance of EnchantmentExecutor.
     * @param player the player who executed
     * @param enchantment the enchantment which was executed
     * @param enchantmentLevel the level of the enchantment
     */
    public EnchantmentExecutor(Player player, Enchantment enchantment, int enchantmentLevel) {
        this.player = player;
        this.enchantment = enchantment;
        this.enchantmentLevel = enchantmentLevel;
    }

    /**
     * Gets the player who executed.
     * @return the executor
     */
    public Player getPlayer() {
        return player;
    }

    /**
     * Gets the enchantment which was executed.
     * @return the enchantment
     */
    public Enchantment getEnchantment() {
        return enchantment;
    }

    /**
     * Gets the level of the enchantment.
     * @return enchantment level
     */
    public int getEnchantmentLevel() {
        return enchantmentLevel;
    }
}
