package org.anhcraft.enc.api.listeners;

import org.anhcraft.enc.api.ActionReport;
import org.bukkit.entity.LivingEntity;

/**
 * The event represents an attack.
 */
public abstract class AttackEvent implements EventListener {
    /**
     * This method is triggered when an entity is being attacked by a player.<br>
     * This method is safe since it has passed strict validations.
     * @param report the report of the attack
     * @param entity the entity
     * @param damage amount of damage
     */
    public abstract void onAttack(ActionReport report, LivingEntity entity, double damage);
}
