package dev.anhcraft.enc.listeners;

import dev.anhcraft.craftkit.events.ArmorUnequipEvent;
import dev.anhcraft.enc.ENC;
import dev.anhcraft.enc.api.EnchantmentAPI;
import dev.anhcraft.enc.api.ItemReport;
import dev.anhcraft.enc.api.listeners.AsyncUnequipListener;
import dev.anhcraft.enc.api.listeners.SyncUnequipListener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class UnequipListener implements Listener {
    @EventHandler
    public void unequip(ArmorUnequipEvent event){
        var player = event.getPlayer();
        var item = event.getArmor();
        var enchants = EnchantmentAPI.listEnchantments(item);
        if(enchants.isEmpty()) return;
        var report = new ItemReport(player, item, enchants);
        var listenerChain = ENC.getTaskChainFactory().newChain();
        enchants.forEach((ench, value) -> {
            if(!ench.isEnabled() || !ench.isAllowedWorld(player.getWorld().getName())) return;
            ench.getEventListeners().stream()
                    .filter(eventListener -> eventListener instanceof SyncUnequipListener)
                    .forEach(eventListener -> {
                        if(eventListener instanceof AsyncUnequipListener) {
                            listenerChain.async(() -> ((AsyncUnequipListener) eventListener)
                                    .onUnequip(report, event));
                        } else{
                            listenerChain.sync(() -> ((SyncUnequipListener) eventListener)
                                    .onUnequip(report, event));
                        }
                    });
        });
        listenerChain.execute();
    }
}
