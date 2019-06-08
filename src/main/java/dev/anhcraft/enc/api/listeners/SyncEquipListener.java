package dev.anhcraft.enc.api.listeners;

import dev.anhcraft.enc.api.ActionReport;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

/**
 * The listener of equip events.
 */
public abstract class SyncEquipListener implements IListener {
    /**
     * This method is called when a player equips an armor.
     * @param report the report
     * @param armor the armor
     * @param slot the slot
     */
    public abstract void onEquip(ActionReport report, ItemStack armor, EquipmentSlot slot);

    @Override
    public boolean canPrevent(){
        return false;
    }

    @Override
    public EquipmentSlot getItemSlot(){
        return null;
    }
}

