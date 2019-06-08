package dev.anhcraft.enc.api.listeners;

import dev.anhcraft.enc.api.ActionReport;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

/**
 * The listener of equip change events.
 */
public abstract class SyncEquipChangeListener implements IListener {
    /**
     * This method is called when a player changes his equipment.
     * @param report the report
     * @param oldArmor the old armor
     * @param newArmor the new armor
     * @param slot the slot
     */
    public abstract void onEquip(ActionReport report, ItemStack oldArmor, ItemStack newArmor, EquipmentSlot slot);

    @Override
    public boolean canPrevent(){
        return false;
    }

    @Override
    public EquipmentSlot getItemSlot(){
        return null;
    }
}

