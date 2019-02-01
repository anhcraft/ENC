package org.anhcraft.enc.api;

import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.LivingEntity;

/**
 * Represents an enchantment for attacks.
 */
public abstract class AttackEnchantment extends Enchantment {
    /**
     * Creates an instance of AttackEnchantment.
     * @param id the enchantment id
     * @param description the description
     * @param author the author
     * @param proposer the proposer (can be null)
     * @param maxLevel the maximum level that a player can enchant up to
     * @param targets item types that may fit the enchantment
     */
    public AttackEnchantment(String id, String[] description, String author, String proposer, int maxLevel, EnchantmentTarget... targets) {
        super(id, description, author, proposer, maxLevel, targets);
    }

    /**
     * This method is triggered whenever an entity is being attacked by a player.<br>
     * This method is safe since it has passed strict validations in order to be called.
     * @param executor the executor
     * @param entity the entity
     * @param damage amount of damage
     */
    public abstract void onAttack(EnchantmentExecutor executor, LivingEntity entity, double damage);
}
