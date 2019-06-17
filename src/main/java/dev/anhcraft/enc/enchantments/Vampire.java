package dev.anhcraft.enc.enchantments;

import dev.anhcraft.enc.api.Enchantment;
import dev.anhcraft.enc.api.ItemReport;
import dev.anhcraft.enc.api.listeners.SyncAttackListener;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import java.util.HashMap;
import java.util.Map;

public class Vampire extends Enchantment {
    public Vampire() {
        super("Vampire", new String[]{
                "When your health is low, there is a chance to heal",
                "yourself a little by using the damage from your attacks"
        }, "anhcraft", null, 5, EnchantmentTarget.WEAPON);

        getEventListeners().add(new SyncAttackListener() {
            @Override
            public void onAttack(ItemReport mainHand, EntityDamageByEntityEvent event, LivingEntity entity) {
                if(event.isCancelled()) return;
                var currentHealth = mainHand.getPlayer().getHealth();
                if(currentHealth > computeConfigValue("low_health", mainHand)) return;
                if(Math.random() > computeConfigValue("chance", mainHand)/100d) return;
                var amount = event.getDamage()*computeConfigValue("damage_percent", mainHand)/100;
                var maxHealth = mainHand.getPlayer().getMaxHealth();
                var newHealth = currentHealth+amount;
                if(newHealth > maxHealth) {
                    amount = maxHealth-currentHealth;
                    newHealth = maxHealth;
                }
                mainHand.getPlayer().setHealth(newHealth);
                event.setDamage(event.getDamage()-amount);
            }
        });
    }

    @Override
    public void onInitConfig(){
        Map<String, Object> map = new HashMap<>();
        map.put("chance", "{level}*5+15");
        map.put("low_health", "7+{level}*0.5");
        map.put("damage_percent", "{level}*10+20");
        initConfigEntries(map);
    }
}
