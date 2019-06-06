package dev.anhcraft.enc.api.listeners;

import dev.anhcraft.enc.api.ActionReport;
import org.anhcraft.spaciouslib.inventory.EquipSlot;
import org.bukkit.block.Block;

/**
 * The listener of block-breaking events.
 */
public abstract class SyncBlockBreakListener implements IListener {
    /**
     * This method is called when a player breaks a block.
     * @param report the report
     * @param block the block
     */
    public abstract void onBreakBlock(ActionReport report, Block block);

    @Override
    public boolean canPrevent(){
        return true;
    }

    @Override
    public EquipSlot getItemSlot(){
        return EquipSlot.MAINHAND;
    }
}

