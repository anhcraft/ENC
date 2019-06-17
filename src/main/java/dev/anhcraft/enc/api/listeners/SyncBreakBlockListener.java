package dev.anhcraft.enc.api.listeners;

import dev.anhcraft.enc.api.ItemReport;
import org.bukkit.event.block.BlockBreakEvent;

/**
 * The listener of {@link BlockBreakEvent}.
 */
public abstract class SyncBreakBlockListener implements IListener {
    /**
     * This method is called when a player breaks a block with his item and the item contains the enchantment.
     * @param mainHand the report of the item in his main hand
     * @param event the event which was fired
     */
    public abstract void onBreakBlock(ItemReport mainHand, BlockBreakEvent event);
}
