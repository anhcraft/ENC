package dev.anhcraft.enc.api.listeners;

import dev.anhcraft.enc.api.ItemReport;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * The listener of item-dropping events which are caused by death.
 */
public abstract class SyncDeathDropListener implements IListener {
    /**
     * This method is called when an item stack which has the enchantment is going to be dropped due to the death of its owner.
     * @param report the report of the item stack
     * @param keep the decision on keeping that item
     */
    public abstract void onDrop(ItemReport report, AtomicBoolean keep);
}
