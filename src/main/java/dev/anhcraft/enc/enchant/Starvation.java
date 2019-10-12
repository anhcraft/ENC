package dev.anhcraft.enc.enchant;

import dev.anhcraft.enc.ENC;
import dev.anhcraft.enc.api.Enchantment;
import dev.anhcraft.enc.api.ItemReport;
import dev.anhcraft.enc.api.handlers.AsyncAttackListener;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashMap;
import java.util.Map;

public class Starvation extends Enchantment {
    public Starvation() {
        super("Starvation", new String[]{
                "There is a chance to make your enemy starve"
        }, "anhcraft", null, 10, EnchantmentTarget.ALL);

        getEnchantHandlers().add(new AsyncAttackListener() {
            @Override
            public void onAttack(ItemReport mainHand, EntityDamageByEntityEvent event, LivingEntity entity) {
                if(event.isCancelled()) return;
                double chance = computeConfigValue("chance", mainHand)/100d;
                if(Math.random() <= chance) {
                    int level = (int) computeConfigValue("effect_level", mainHand);
                    int duration = (int) computeConfigValue("effect_duration", mainHand);
                    ENC.getTaskChainFactory().newChain().sync(() -> entity.addPotionEffect(new PotionEffect(PotionEffectType.HUNGER, duration, level))).execute();
                }
            }
        });
    }

    @Override
    public void onInitConfig(){
        Map<String, Object> map = new HashMap<>();
        map.put("chance", "{level}*1.5+20");
        map.put("effect_level", "ceil({level}*0.3)");
        map.put("effect_duration", "{level}*10+50");
        initConfigEntries(map);
    }
}
