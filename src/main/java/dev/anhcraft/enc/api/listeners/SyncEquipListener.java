package dev.anhcraft.enc.api.listeners;

import dev.anhcraft.craftkit.events.ArmorEquipEvent;
import dev.anhcraft.enc.api.ItemReport;

/**
 * The listener of {@link ArmorEquipEvent}.
 */
public abstract class SyncEquipListener implements IListener {
    /**
     * This method is called when a player equips an armor which has the enchantment.
     * @param equip the report of the equipment
     * @param event the event which was fired
     */
    public abstract void onEquip(ItemReport equip, ArmorEquipEvent event);
}
