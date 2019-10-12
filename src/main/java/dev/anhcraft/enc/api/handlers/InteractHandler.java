package dev.anhcraft.enc.api.handlers;

import dev.anhcraft.enc.api.ItemReport;
import org.bukkit.event.player.PlayerInteractEvent;

/**
 * The listener of {@link PlayerInteractEvent}.
 */
public abstract class InteractHandler implements EnchantHandler {
    /**
     * This method is called when a player interacts something with his item and the item has the enchantment.
     * @param report the report of the item
     * @param event the event which was fired
     */
    public abstract void onInteract(ItemReport report, PlayerInteractEvent event);
}
