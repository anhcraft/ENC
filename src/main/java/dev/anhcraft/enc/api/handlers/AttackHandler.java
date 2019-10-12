package dev.anhcraft.enc.api.handlers;

import dev.anhcraft.enc.api.ItemReport;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

/**
 * The listener of {@link EntityDamageByEntityEvent}.
 */
public abstract class AttackHandler implements EnchantHandler {
    /**
     * This method is called when a player attacks another entity with his item and the item has the enchantment.
     * @param mainHand the report of the item in his main hand
     * @param event the event which was fired
     * @param entity the entity who was attacked
     */
    public abstract void onAttack(ItemReport mainHand, EntityDamageByEntityEvent event, LivingEntity entity);
}
