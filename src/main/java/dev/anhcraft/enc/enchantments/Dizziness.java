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

public class Dizziness extends Enchantment {
    public Dizziness() {
        super("Dizziness", new String[]{
                "Has a chance to make someone feel dizzy"
        }, "anhcraft", null, 10, EnchantmentTarget.ALL);

        getEventListeners().add(new AsyncAttackListener() {
            @Override
            public void onAttack(ItemReport mainHand, EntityDamageByEntityEvent event, LivingEntity entity) {
                if(event.isCancelled()) return;
                double chance = computeConfigValue("chance", mainHand)/100d;
                if(Math.random() > chance) return;
                int level = (int) computeConfigValue("effect_level", mainHand);
                int duration = (int) computeConfigValue("effect_duration", mainHand);
                ENC.getTaskChainFactory().newChain().sync(() -> entity.addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, duration, level))).execute();
            }
        });
    }

    @Override
    public void onInitConfig(){
        Map<String, Object> map = new HashMap<>();
        map.put("chance", "{level}*3.5");
        map.put("effect_level", "ceil({level}*0.4)");
        map.put("effect_duration", "{level}*10+30");
        initConfigEntries(map);
    }
}
