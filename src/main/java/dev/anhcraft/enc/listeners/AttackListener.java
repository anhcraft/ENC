package dev.anhcraft.enc.listeners;

import dev.anhcraft.craftkit.utils.ItemUtil;
import dev.anhcraft.enc.ENC;
import dev.anhcraft.enc.api.EnchantmentAPI;
import dev.anhcraft.enc.api.ItemReport;
import dev.anhcraft.enc.api.listeners.AsyncAttackListener;
import dev.anhcraft.enc.api.listeners.SyncAttackListener;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class AttackListener implements Listener {
    @EventHandler
    public void attack(EntityDamageByEntityEvent event){
        if(event.getDamager() instanceof Player && event.getEntity() instanceof LivingEntity){
            var damager = (Player) event.getDamager();
            var item = damager.getInventory().getItemInMainHand();
            if(ItemUtil.isNull(item)) return;
            var enchants = EnchantmentAPI.listEnchantments(item);
            if(enchants.isEmpty()) return;
            var entity = (LivingEntity) event.getEntity();
            var report = new ItemReport(damager, item, enchants);
            var listenerChain = ENC.getTaskChainFactory().newChain();
            enchants.forEach((ench, value) -> {
                if(!ench.isEnabled() || !ench.isAllowedWorld(damager.getWorld().getName())) return;
                ench.getEventListeners().stream()
                        .filter(eventListener -> eventListener instanceof SyncAttackListener)
                        .forEach(eventListener -> {
                            if(eventListener instanceof AsyncAttackListener) {
                                listenerChain.async(() -> ((AsyncAttackListener) eventListener)
                                        .onAttack(report, event, entity));
                            } else{
                                listenerChain.sync(() -> ((SyncAttackListener) eventListener)
                                        .onAttack(report, event, entity));
                            }
                        });
            });
            listenerChain.execute();
        }
    }
}
