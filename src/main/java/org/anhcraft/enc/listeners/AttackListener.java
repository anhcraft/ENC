package org.anhcraft.enc.listeners;

import co.aikar.taskchain.TaskChain;
import org.anhcraft.enc.ENC;
import org.anhcraft.enc.api.ActionReport;
import org.anhcraft.enc.api.Enchantment;
import org.anhcraft.enc.api.listeners.AsyncAttackListener;
import org.anhcraft.enc.api.listeners.SyncAttackListener;
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
                if(enchants.isEmpty()){
                    return;
                }
                ActionReport report = new ActionReport(damager, item, enchants, event.isCancelled());
                TaskChain<Boolean> listenerChain = ENC.getTaskChainFactory().newChain();
                LivingEntity entity = (LivingEntity) event.getEntity();
                for(Map.Entry<Enchantment, Integer> e : enchants.entrySet()) {
                    Enchantment enchantment = e.getKey();
                    if(!enchantment.isEnabled() ||
                            !enchantment.isAllowedWorld(damager.getWorld().getName())) {
                        continue;
                    }
                    enchantment.getEventListeners().stream()
                        .filter(eventListener -> eventListener instanceof SyncAttackListener)
                        .forEach(eventListener -> {
                            if(eventListener instanceof AsyncAttackListener) {
                                listenerChain.async(() -> ((AsyncAttackListener) eventListener)
                                        .onAttack(report, entity, event.getDamage()));
                            } else{
                                listenerChain.sync(() -> ((SyncAttackListener) eventListener)
                                        .onAttack(report, entity, event.getDamage()));
                            }
                        });
                }
                listenerChain.execute();
                event.setCancelled(report.isPrevented());
            }
        }
    }
}
