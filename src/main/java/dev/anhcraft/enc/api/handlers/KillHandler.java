package dev.anhcraft.enc.api.handlers;

import dev.anhcraft.enc.api.ItemReport;
import org.bukkit.event.entity.EntityDeathEvent;

/**
 * The listener of kill events.
 */
public abstract class KillHandler implements EnchantHandler {
    /**
     * This method is called when a player kills another entity with his item and the item has the enchantment.
     * @param mainHand the report of the item in his main hand
     * @param event the event which was fired
     */
    public abstract void onKill(ItemReport mainHand, EntityDeathEvent event);
}
