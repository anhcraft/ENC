package dev.anhcraft.enc.api.listeners;

import org.bukkit.inventory.EquipmentSlot;
import org.jetbrains.annotations.Nullable;

/**
 * Interface of event listeners.
 */
public interface IListener {
    /**
     * Returns whether this listener can prevent the event by using {@link dev.anhcraft.enc.api.ActionReport#setPrevented(boolean)}.
     * @return true if yes
     */
    boolean canPrevent();

    /**
     * Gets the slot of the validated stack of items.
     * @return the slot (may be null if the slot can be anywhere on the whole inventory)
     */
    @Nullable
    EquipmentSlot getItemSlot();
}
