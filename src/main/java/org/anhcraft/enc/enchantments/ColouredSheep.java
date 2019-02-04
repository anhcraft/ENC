package org.anhcraft.enc.enchantments;

import org.anhcraft.enc.api.AttackEnchantment;
import org.anhcraft.enc.api.EnchantmentExecutor;
import org.anhcraft.spaciouslib.utils.RandomUtils;
import org.bukkit.DyeColor;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Sheep;
import org.bukkit.inventory.ItemStack;

public class ColouredSheep extends AttackEnchantment {
    public ColouredSheep() {
        super("ColouredSheep", new String[]{
            "Colour the target sheep every attacks"
        }, "anhcraft", null, 1, EnchantmentTarget.ALL);
    }

    @Override
    public void onAttack(EnchantmentExecutor executor, LivingEntity entity, double damage) {
        if(entity instanceof Sheep){
            Sheep s = (Sheep) entity;
            if(!s.isSheared()){
                s.setColor(RandomUtils.pickRandom(DyeColor.values()));
            }
        }
    }

    @Override
    public boolean canEnchantItem(ItemStack itemStack) {
        return true;
    }
}
