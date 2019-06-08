package dev.anhcraft.enc.api.listeners;

import dev.anhcraft.enc.api.ActionReport;
import org.bukkit.inventory.EquipmentSlot;

/**
 * The listener of jumping events.
 */
public abstract class SyncJumpListener implements IListener {
    /**
     * This method is called when a player jumps.
     * @param report the report
     * @param onSpot whether the player jumps on spot
     */
    public abstract void onJump(ActionReport report, boolean onSpot);

    @Override
    public boolean canPrevent(){
        return false;
    }

    @Override
    public EquipmentSlot getItemSlot(){
        return EquipmentSlot.FEET;
    }
}

