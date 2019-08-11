package dev.anhcraft.enc.enchantments;

import dev.anhcraft.enc.api.Enchantment;
import dev.anhcraft.enc.api.ItemReport;
import dev.anhcraft.enc.api.listeners.SyncAttackListener;
import dev.anhcraft.jvmkit.utils.RandomUtil;
import org.bukkit.DyeColor;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Sheep;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class ColouredSheep extends Enchantment {
    public ColouredSheep() {
        super("ColouredSheep", new String[]{
            "Colours the target sheep every attack"
        }, "anhcraft", null, 1, EnchantmentTarget.ALL);

        // we will make modification so that we must use the sync event
        getEventListeners().add(new SyncAttackListener() {
            @Override
            public void onAttack(ItemReport mainHand, EntityDamageByEntityEvent event, LivingEntity entity) {
                if(!event.isCancelled() && entity instanceof Sheep){
                    Sheep s = (Sheep) entity;
                    if(!s.isSheared()) s.setColor(
                            DyeColor.values()[RandomUtil.randomInt(0, DyeColor.values().length-1)]);
                }
            }
        });
    }
}
