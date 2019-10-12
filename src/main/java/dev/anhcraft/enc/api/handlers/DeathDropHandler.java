package dev.anhcraft.enc.api.handlers;

import dev.anhcraft.enc.api.ItemReport;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * The listener of item-dropping events which are caused by death.
 */
public abstract class DeathDropHandler implements EnchantHandler {
    /**
     * This method is called when an item stack which has the enchantment is going to be dropped due to the death of its owner.
     * @param report the report of the item stack
     * @param shouldKeep the decision on keeping that item
     */
    public abstract void onDrop(ItemReport report, AtomicBoolean shouldKeep);
}
