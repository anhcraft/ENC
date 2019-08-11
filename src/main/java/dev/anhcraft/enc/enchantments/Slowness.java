package dev.anhcraft.enc.enchantments;

import dev.anhcraft.enc.ENC;
import dev.anhcraft.enc.api.Enchantment;
import dev.anhcraft.enc.api.ItemReport;
import dev.anhcraft.enc.api.listeners.AsyncAttackListener;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashMap;
import java.util.Map;

public class Slowness extends Enchantment {
    public Slowness() {
        super("Slowness", new String[]{
                "There is a chance to slow someone else"
        }, "anhcraft", null, 10, EnchantmentTarget.ALL);

        getEventListeners().add(new AsyncAttackListener() {
            @Override
            public void onAttack(ItemReport mainHand, EntityDamageByEntityEvent event, LivingEntity entity) {
                if(event.isCancelled()) return;
                double chance = computeConfigValue("chance", mainHand)/100d;
                if(Math.random() <= chance) {
                    int level = (int) computeConfigValue("effect_level", mainHand);
                    int duration = (int) computeConfigValue("effect_duration", mainHand);
                    ENC.getTaskChainFactory().newChain().sync(() -> entity.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, duration, level))).execute();
                }
            }
        });
    }

    @Override
    public void onInitConfig(){
        Map<String, Object> map = new HashMap<>();
        map.put("chance", "{level}*3.7");
        map.put("effect_level", "ceil({level}*0.5)");
        map.put("effect_duration", "{level}*12+30");
        initConfigEntries(map);
    }
}
