package dev.anhcraft.enc.api.listeners;

import dev.anhcraft.craftkit.events.PlayerJumpEvent;
import dev.anhcraft.enc.api.ItemReport;

/**
 * The listener of {@link PlayerJumpEvent}.
 */
public abstract class SyncJumpListener implements IListener {
    /**
     * This method is called when a player jumps and his boot contains the enchantment.
     * @param foot the report of the item on this foot
     * @param event the event which was fired
     */
    public abstract void onJump(ItemReport foot, PlayerJumpEvent event);
}
