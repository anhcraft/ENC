package org.anhcraft.enc.api.listeners;

import org.anhcraft.enc.api.ActionReport;
import org.bukkit.entity.LivingEntity;

/**
 * Represents the listener of attack event which is fired synchronously.
 */
public abstract class SyncAttackListener implements IListener {
    /**
     * This method is called when the attack event happens and the listener determines that the event is related to enchantment.
     * @param report the report of the attack
     * @param entity the entity
     * @param damage amount of damage
     */
    public abstract void onAttack(ActionReport report, LivingEntity entity, double damage);
}
