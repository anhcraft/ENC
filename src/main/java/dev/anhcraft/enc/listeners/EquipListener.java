package dev.anhcraft.enc.listeners;

import dev.anhcraft.craftkit.events.ArmorEquipEvent;
import dev.anhcraft.enc.ENC;
import dev.anhcraft.enc.api.EnchantmentAPI;
import dev.anhcraft.enc.api.ItemReport;
import dev.anhcraft.enc.api.listeners.AsyncEquipListener;
import dev.anhcraft.enc.api.listeners.SyncEquipListener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class EquipListener implements Listener {
    @EventHandler
    public void equip(ArmorEquipEvent event){
        var player = event.getPlayer();
        var item = event.getArmor();
        var enchants = EnchantmentAPI.listEnchantments(item);
        if(enchants.isEmpty()) return;
        var report = new ItemReport(player, item, enchants);
        var listenerChain = ENC.getTaskChainFactory().newChain();
        enchants.forEach((ench, value) -> {
            if(!ench.isEnabled() || !ench.isAllowedWorld(player.getWorld().getName())) return;
            ench.getEventListeners().stream()
                    .filter(eventListener -> eventListener instanceof SyncEquipListener)
                    .forEach(eventListener -> {
                        if(eventListener instanceof AsyncEquipListener) {
                            listenerChain.async(() -> ((AsyncEquipListener) eventListener)
                                    .onEquip(report, event));
                        } else{
                            listenerChain.sync(() -> ((SyncEquipListener) eventListener)
                                    .onEquip(report, event));
                        }
                    });
        });
        listenerChain.execute();
    }
}
