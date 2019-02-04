package org.anhcraft.enc.api.listeners;

import org.anhcraft.enc.api.EnchantmentExecutor;
import org.bukkit.entity.LivingEntity;

/**
 * The event represents an attack.
 */
public abstract class AttackEvent implements EventListener {
    /**
     * This method is triggered when an entity is being attacked by a player.<br>
     * This method is safe since it has passed strict validations.
     * @param executor the executor
     * @param entity the entity
     * @param damage amount of damage
     */
    public abstract void onAttack(EnchantmentExecutor executor, LivingEntity entity, double damage);
}
