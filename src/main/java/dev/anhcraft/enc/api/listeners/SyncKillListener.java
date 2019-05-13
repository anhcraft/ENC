package dev.anhcraft.enc.api.listeners;

import dev.anhcraft.enc.api.ActionReport;
import org.anhcraft.spaciouslib.inventory.EquipSlot;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;

import java.util.List;

/**
 * The listener of kill events.
 */
public abstract class SyncKillListener implements IListener {
    /**
     * This method is called when a player kills an entity.
     * @param report the report
     * @param entity the entity
     * @param drops stacks of items which will drop when the entity dies
     */
    public abstract void onKill(ActionReport report, LivingEntity entity, List<ItemStack> drops);

    @Override
    public boolean canPrevent(){
        return false;
    }

    @Override
    public EquipSlot getItemSlot(){
        return EquipSlot.MAINHAND;
    }
}

