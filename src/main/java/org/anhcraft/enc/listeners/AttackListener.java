package org.anhcraft.enc.listeners;

import org.anhcraft.enc.ENC;
import org.anhcraft.enc.api.AttackEnchantment;
import org.anhcraft.enc.api.Enchantment;
import org.anhcraft.enc.api.EnchantmentExecutor;
import org.anhcraft.spaciouslib.utils.InventoryUtils;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class AttackListener implements Listener {
    @EventHandler
    public void attack(EntityDamageByEntityEvent event){
        if(event.getDamager() instanceof Player && event.getEntity() instanceof LivingEntity){
            Player damager = (Player) event.getDamager();
            ItemStack item = damager.getInventory().getItemInMainHand();
            if(!InventoryUtils.isNull(item)) {
                HashMap<Enchantment, Integer> enchants = ENC.getApi().listEnchantments(item);
                for(Map.Entry<Enchantment, Integer> e : enchants.entrySet()) {
                    if(e.getKey() instanceof AttackEnchantment) {
                        if(e.getKey().isEnabled() && e.getKey().isAllowedWorld(damager.getWorld().getName())) {
                            ((AttackEnchantment) e.getKey()).onAttack(
                                    new EnchantmentExecutor(damager, e.getKey(), e.getValue()),
                                    (LivingEntity) event.getEntity(), event.getDamage());
                        }
                    }
                }
            }
        }
    }
}
