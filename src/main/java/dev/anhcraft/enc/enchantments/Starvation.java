package dev.anhcraft.enc.enchantments;

import dev.anhcraft.enc.ENC;
import dev.anhcraft.enc.api.ActionReport;
import dev.anhcraft.enc.api.Enchantment;
import dev.anhcraft.enc.api.listeners.AsyncAttackListener;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashMap;
import java.util.Map;

public class Starvation extends Enchantment {
    public Starvation() {
        super("Starvation", new String[]{
                "There is a chance to make your enemy starve"
        }, "anhcraft", null, 10, EnchantmentTarget.ALL);

        getEventListeners().add(new AsyncAttackListener() {
            @Override
            public void onAttack(ActionReport report, LivingEntity entity, double damage) {
                if(report.isPrevented()) return;
                var chance = computeConfigValue("chance", report)/100d;
                if(Math.random() <= chance){
                    var level = (int) computeConfigValue("effect_level", report);
                    var duration = (int) computeConfigValue("effect_duration", report);
                    ENC.getTaskChainFactory().newChain().sync(() ->
                            entity.addPotionEffect(new PotionEffect(PotionEffectType.HUNGER, duration, level))
                    ).execute();
                }
            }
        });
    }

    @Override
    public void onConfigReloaded(){
        Map<String, Object> map = new HashMap<>();
        map.put("chance", "{level}*1.5+20");
        map.put("effect_level", "ceil({level}*0.3)");
        map.put("effect_duration", "{level}*10+50");
        initConfigEntries(map);
    }
}
