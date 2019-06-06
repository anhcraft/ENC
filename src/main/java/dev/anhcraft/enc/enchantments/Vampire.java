package dev.anhcraft.enc.enchantments;

import dev.anhcraft.enc.api.ActionReport;
import dev.anhcraft.enc.api.Enchantment;
import dev.anhcraft.enc.api.listeners.AsyncAttackListener;
import org.anhcraft.spaciouslib.attribute.Attribute;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.LivingEntity;

import java.util.HashMap;

public class Vampire extends Enchantment {
    public Vampire() {
        super("Vampire", new String[]{
                "When your health is low, there is a chance to heal",
                "yourself a little using the damage within an attack"
        }, "anhcraft", null, 5, EnchantmentTarget.WEAPON);

        getEventListeners().add(new AsyncAttackListener() {
            @Override
            public void onAttack(ActionReport report, LivingEntity entity, double damage) {
                if(report.isPrevented()){
                    return;
                }
                double currentHealth = report.getPlayer().getHealth();
                if(currentHealth > computeConfigValue("low_health", report)){
                    return;
                }
                if(Math.random() > computeConfigValue("chance", report)/100d){
                    return;
                }
                double amount = damage*computeConfigValue("damage_percent", report)/100;
                double maxHealth = Attribute.Type.GENERIC_MAX_HEALTH.getBaseValue();
                double newHealth = currentHealth+amount;
                if(newHealth > maxHealth){
                    newHealth = maxHealth;
                }
                report.getPlayer().setHealth(newHealth);
            }
        });
    }

    @Override
    public void onRegistered(){
        HashMap<String, Object> map = new HashMap<>();
        map.put("chance", "{level}*5+15");
        map.put("low_health", "7+{level}*0.5");
        map.put("damage_percent", "{level}*10+20");
        initConfigEntries(map);
    }
}
