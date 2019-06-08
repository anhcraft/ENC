package dev.anhcraft.enc.api.listeners;

import dev.anhcraft.enc.api.ActionReport;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

/**
 * The listener of item drop events which are caused by death.
 */
public abstract class SyncDeathDropListener implements IListener {
    /**
     * This method is called when a stack of items is going to be dropped due to the death of its owner.
     * @param report the report
     * @param itemStack the stack of items
     */
    public abstract void onDrop(ActionReport report, ItemStack itemStack);

    @Override
    public boolean canPrevent(){
        return true;
    }

    @Override
    public EquipmentSlot getItemSlot(){
        return null;
    }
}

