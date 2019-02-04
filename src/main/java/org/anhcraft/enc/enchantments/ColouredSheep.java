package org.anhcraft.enc.enchantments;

import org.anhcraft.enc.api.ActionReport;
import org.anhcraft.enc.api.Enchantment;
import org.anhcraft.enc.api.listeners.AttackEvent;
import org.anhcraft.spaciouslib.utils.RandomUtils;
import org.bukkit.DyeColor;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Sheep;
import org.bukkit.inventory.ItemStack;

public class ColouredSheep extends Enchantment {
    public ColouredSheep() {
        super("ColouredSheep", new String[]{
            "Colour the target sheep every attacks"
        }, "anhcraft", null, 1, EnchantmentTarget.ALL);

        getEventListeners().add(new AttackEvent() {
            @Override
            public void onAttack(ActionReport report, LivingEntity entity, double damage) {
                if(entity instanceof Sheep){
                    Sheep s = (Sheep) entity;
                    if(!s.isSheared()){
                        s.setColor(RandomUtils.pickRandom(DyeColor.values()));
                    }
                }
            }
        });
    }

    @Override
    public boolean canEnchantItem(ItemStack itemStack) {
        return true;
    }
}
