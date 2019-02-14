package org.anhcraft.enc.enchantments;

import org.anhcraft.enc.ENC;
import org.anhcraft.enc.api.ActionReport;
import org.anhcraft.enc.api.Enchantment;
import org.anhcraft.enc.api.listeners.AsyncAttackListener;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashMap;

public class Slowness extends Enchantment {
    public Slowness() {
        super("Slowness", new String[]{
                "There is a chance to slow someone else"
        }, "anhcraft", null, 10, EnchantmentTarget.ALL);

        getEventListeners().add(new AsyncAttackListener() {
            @Override
            public void onAttack(ActionReport report, LivingEntity entity, double damage) {
                if(report.isPrevented()){
                    return;
                }
                double chance = computeConfigValue("chance", report)/100d;
                if(Math.random() <= chance) {
                    int level = (int) Math.ceil(computeConfigValue("effect_level", report));
                    int duration = (int) Math.ceil(computeConfigValue("effect_duration", report));
                    ENC.getTaskChainFactory().newChain().sync(() ->
                            entity.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, duration, level))
                    ).execute();
                }
            }
        });
    }

    @Override
    public void onRegistered(){
        HashMap<String, Object> map = new HashMap<>();
        map.put("chance", "{level}*3.7");
        map.put("effect_level", "{level}*0.5");
        map.put("effect_duration", "{level}*12+30");
        initConfigEntries(map);
    }

    @Override
    public boolean canEnchantItem(ItemStack itemStack) {
        return true;
    }
}
