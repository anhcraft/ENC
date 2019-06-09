package dev.anhcraft.enc.api.listeners;

import dev.anhcraft.enc.api.ActionReport;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.inventory.EquipmentSlot;

/**
 * The listener of interacting event.
 */
public abstract class SyncInteractListener implements IListener {
    /**
     * This method is called when a player interacts something with his item.
     * @param report the report
     * @param player the player
     * @param action type of the action
     * @param hand the hand which he held the item
     * @param block the block which related to this event (may be null)
     */
    public abstract void onInteract(ActionReport report, Player player, Action action, EquipmentSlot hand, Block block);

    @Override
    public boolean canPrevent(){
        return true;
    }

    @Override
    public EquipmentSlot getItemSlot(){
        return null;
    }
}
