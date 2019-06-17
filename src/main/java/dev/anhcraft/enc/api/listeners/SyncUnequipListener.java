package dev.anhcraft.enc.api.listeners;

import dev.anhcraft.craftkit.events.ArmorUnequipEvent;
import dev.anhcraft.enc.api.ItemReport;

/**
 * The listener of {@link ArmorUnequipEvent}.
 */
public abstract class SyncUnequipListener implements IListener {
    /**
     * This method is called when a player unequips an armor which has the enchantment.
     * @param equip the report of the equipment
     * @param event the event which was fired
     */
    public abstract void onUnequip(ItemReport equip, ArmorUnequipEvent event);
}
