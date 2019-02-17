package org.anhcraft.enc.api.listeners;

import org.anhcraft.spaciouslib.inventory.EquipSlot;

/**
 * Interface of event listeners.
 */
public interface IListener {
    /**
     * Returns whether this listener can be prevented by using {@link org.anhcraft.enc.api.ActionReport#setPrevented(boolean)}.
     * @return true if yes
     */
    boolean canPrevent();

    /**
     * Gets the slot of the validated stack of items.
     * @return the slot (may be null if the slot can be anywhere on the whole inventory)
     */
    EquipSlot getItemSlot();
}
