package dev.anhcraft.enc.api.listeners;

import dev.anhcraft.enc.api.ActionReport;
import org.anhcraft.spaciouslib.inventory.EquipSlot;
import org.bukkit.entity.LivingEntity;

/**
 * The listener of attack events.
 */
public abstract class SyncAttackListener implements IListener {
    /**
     * This method is called when a player attacks an entity.
     * @param report the report
     * @param entity the entity
     * @param damage amount of damage
     */
    public abstract void onAttack(ActionReport report, LivingEntity entity, double damage);

    @Override
    public boolean canPrevent(){
        return true;
    }

    @Override
    public EquipSlot getItemSlot(){
        return EquipSlot.MAINHAND;
    }
}
