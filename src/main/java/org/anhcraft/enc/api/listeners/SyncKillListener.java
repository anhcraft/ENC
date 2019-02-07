package org.anhcraft.enc.api.listeners;

import org.anhcraft.enc.api.ActionReport;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;

import java.util.List;

/**
 * Represents the listener of kill event which is fired synchronously.
 */
public abstract class SyncKillListener implements IListener {
    /**
     * This method is called when the kill event happens and the listener determines that the event is related to enchantment.
     * @param report the report of the kill
     * @param entity the entity
     * @param drops stacks of items which will drop when the entity dies
     */
    public abstract void onAttack(ActionReport report, LivingEntity entity, List<ItemStack> drops);
}

