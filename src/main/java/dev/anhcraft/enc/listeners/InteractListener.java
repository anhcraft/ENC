package dev.anhcraft.enc.listeners;

import co.aikar.taskchain.TaskChain;
import dev.anhcraft.craftkit.utils.ItemUtil;
import dev.anhcraft.enc.ENC;
import dev.anhcraft.enc.api.ActionReport;
import dev.anhcraft.enc.api.EnchantmentAPI;
import dev.anhcraft.enc.api.listeners.AsyncInteractListener;
import dev.anhcraft.enc.api.listeners.SyncInteractListener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;

public class InteractListener implements Listener {
    @EventHandler
    public void interact(PlayerInteractEvent event){
        var item = event.getItem();
        if(!ItemUtil.isNull(item)) {
            var enchants = EnchantmentAPI.listEnchantments(item);
            if(enchants.isEmpty()) return;
            var report = new ActionReport(event.getPlayer(), item, enchants, event.isCancelled());
            TaskChain<Boolean> listenerChain = ENC.getTaskChainFactory().newChain();
            for(var e : enchants.entrySet()) {
                var enchantment = e.getKey();
                if(!enchantment.isEnabled() || !enchantment.isAllowedWorld(event.getPlayer().getWorld().getName())) continue;
                enchantment.getEventListeners().stream()
                        .filter(eventListener -> eventListener instanceof SyncInteractListener)
                        .forEach(eventListener -> {
                            if(eventListener instanceof AsyncInteractListener) {
                                listenerChain.async(() -> ((AsyncInteractListener) eventListener)
                                        .onInteract(report, event.getPlayer(), event.getAction(),
                                                event.getHand(), event.getClickedBlock()));
                            } else{
                                listenerChain.sync(() -> ((SyncInteractListener) eventListener)
                                        .onInteract(report, event.getPlayer(), event.getAction(),
                                                event.getHand(), event.getClickedBlock()));
                            }
                        });
            }
            listenerChain.execute();
            event.setCancelled(report.isPrevented());
        }
    }
}
