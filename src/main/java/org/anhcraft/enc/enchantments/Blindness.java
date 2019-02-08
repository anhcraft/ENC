package org.anhcraft.enc.enchantments;

import org.anhcraft.enc.ENC;
import org.anhcraft.enc.api.ActionReport;
import org.anhcraft.enc.api.Enchantment;
import org.anhcraft.enc.api.listeners.SyncAttackListener;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashMap;

public class Blindness extends Enchantment {
    public Blindness() {
        super("Blindness", new String[]{
                "There is an opportunity for you to make your enemy blind"
        }, "anhcraft", null, 10, EnchantmentTarget.ALL);

        getEventListeners().add(new SyncAttackListener() {
            @Override
            public void onAttack(ActionReport report, LivingEntity entity, double damage) {
                if(report.isPrevented()){
                    return;
                }
                double chance = computeConfigValue("chance", report)/100d;
                if(Math.random() > chance) {
                    return;
                }
                int level = (int) Math.ceil(computeConfigValue("effect_level", report));
                int duration = (int) Math.ceil(computeConfigValue("effect_duration", report));
                ENC.getTaskChainFactory().newChain().sync(() ->
                        entity.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, duration, level))
                ).execute();
            }
        });
    }

    @Override
    public void onRegistered(){
        HashMap<String, Object> map = new HashMap<>();
        map.put("chance", "{level}*3.2");
        map.put("effect_level", "1");
        map.put("effect_duration", "{level}*20+30");
        initConfigEntries(map);
    }

    @Override
    public boolean canEnchantItem(ItemStack itemStack) {
        return true;
    }
}
