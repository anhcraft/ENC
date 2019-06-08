package dev.anhcraft.enc.listeners;

import dev.anhcraft.craftkit.utils.ItemUtil;
import dev.anhcraft.enc.ENC;
import dev.anhcraft.enc.api.ActionReport;
import dev.anhcraft.enc.api.EnchantmentAPI;
import dev.anhcraft.enc.api.listeners.AsyncKillListener;
import dev.anhcraft.enc.api.listeners.SyncKillListener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;

public class KillListener implements Listener {
    @EventHandler
    public void death(EntityDeathEvent event){
        if(event.getEntity().getKiller() != null){
            var killer = event.getEntity().getKiller();
            var item = killer.getInventory().getItemInMainHand();
            if(!ItemUtil.isNull(item)) {
                var enchants = EnchantmentAPI.listEnchantments(item);
                if(enchants.isEmpty()) return;
                var report = new ActionReport(killer, item, enchants, false);
                var listenerChain = ENC.getTaskChainFactory().newChain();
                for(var e : enchants.entrySet()) {
                    var enchantment = e.getKey();
                    if(!enchantment.isEnabled() || !enchantment.isAllowedWorld(killer.getWorld().getName())) continue;
                    enchantment.getEventListeners().stream()
                        .filter(eventListener -> eventListener instanceof SyncKillListener)
                        .forEach(eventListener -> {
                            if(eventListener instanceof AsyncKillListener) {
                                listenerChain.async(() -> ((AsyncKillListener) eventListener)
                                        .onKill(report, event.getEntity(), event.getDrops()));
                            } else{
                                listenerChain.sync(() -> ((SyncKillListener) eventListener)
                                        .onKill(report, event.getEntity(), event.getDrops()));
                            }
                        });
                }
                listenerChain.execute();
            }
        }
    }
}
