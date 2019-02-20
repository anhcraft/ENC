package org.anhcraft.enc.api.listeners;

import org.anhcraft.enc.api.ActionReport;
import org.anhcraft.spaciouslib.inventory.EquipSlot;
import org.bukkit.inventory.ItemStack;

/**
 * The listener of equip events.
 */
public abstract class SyncEquipListener implements IListener {
    /**
     * This method is called when a player equips an armor.
     * @param report the report
     * @param oldArmor the old armor
     * @param newArmor the new armor
     * @param slot the slot
     */
    public abstract void onEquip(ActionReport report, ItemStack oldArmor, ItemStack newArmor, EquipSlot slot);

    @Override
    public boolean canPrevent(){
        return false;
    }

    @Override
    public EquipSlot getItemSlot(){
        return null;
    }
}

