package dev.anhcraft.enc.api.handlers;

import dev.anhcraft.craftkit.events.ArmorChangeEvent;
import dev.anhcraft.enc.api.ItemReport;

/**
 * The listener of {@link ArmorChangeEvent}.
 */
public abstract class ChangeEquipHandler implements EnchantHandler {
    /**
     * This method is called when a player changes his armor and either old or new ones have the enchantment.
     * @param oldEquip the old equipment
     * @param newEquip the new equipment
     * @param event the event which was fired
     * @param onOldEquip {@code true} if the enchantment is on the old equipment, otherwise is on the new one
     */
    public abstract void onChangeEquip(ItemReport oldEquip, ItemReport newEquip, ArmorChangeEvent event, boolean onOldEquip);
}
