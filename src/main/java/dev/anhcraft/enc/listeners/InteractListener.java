package dev.anhcraft.enc.listeners;

import dev.anhcraft.craftkit.utils.ItemUtil;
import dev.anhcraft.enc.ENC;
import dev.anhcraft.enc.api.EnchantmentAPI;
import dev.anhcraft.enc.api.ItemReport;
import dev.anhcraft.enc.api.listeners.AsyncInteractListener;
import dev.anhcraft.enc.api.listeners.SyncInteractListener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;

public class InteractListener implements Listener {
    @EventHandler
    public void interact(PlayerInteractEvent event){
        var player = event.getPlayer();
        var item = event.getItem();
        if(ItemUtil.isNull(item)) return;
        var enchants = EnchantmentAPI.listEnchantments(item);
        if(enchants.isEmpty()) return;
        var report = new ItemReport(player, item, enchants);
        var listenerChain = ENC.getTaskChainFactory().newChain();
        enchants.forEach((ench, value) -> {
            if(!ench.isEnabled() || !ench.isAllowedWorld(player.getWorld().getName())) return;
            ench.getEventListeners().stream()
                    .filter(eventListener -> eventListener instanceof SyncInteractListener)
                    .forEach(eventListener -> {
                        if(eventListener instanceof AsyncInteractListener) {
                            listenerChain.async(() -> ((AsyncInteractListener) eventListener)
                                    .onInteract(report, event));
                        } else{
                            listenerChain.sync(() -> ((SyncInteractListener) eventListener)
                                    .onInteract(report, event));
                        }
                    });
        });
        listenerChain.execute();
    }
}
