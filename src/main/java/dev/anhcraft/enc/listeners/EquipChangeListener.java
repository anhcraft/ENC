package dev.anhcraft.enc.listeners;

import dev.anhcraft.craftkit.events.ArmorChangeEvent;
import dev.anhcraft.enc.ENC;
import dev.anhcraft.enc.api.EnchantmentAPI;
import dev.anhcraft.enc.api.ItemReport;
import dev.anhcraft.enc.api.listeners.AsyncChangeEquipListener;
import dev.anhcraft.enc.api.listeners.SyncChangeEquipListener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class EquipChangeListener implements Listener {
    @EventHandler
    public void changeEquip(ArmorChangeEvent event){
        var player = event.getPlayer();
        var oldItem = event.getOldArmor();
        var oldEnchants = EnchantmentAPI.listEnchantments(oldItem);
        var newItem = event.getNewArmor();
        var newEnchants = EnchantmentAPI.listEnchantments(newItem);
        var listenerChain = ENC.getTaskChainFactory().newChain();
        var oldReport = new ItemReport(player, oldItem, oldEnchants);
        var newReport = new ItemReport(player, newItem, newEnchants);
        oldEnchants.forEach((ench, e) -> {
            if(!ench.isEnabled() || !ench.isAllowedWorld(player.getWorld().getName())) return;
            ench.getEventListeners().stream()
                    .filter(eventListener -> eventListener instanceof SyncChangeEquipListener)
                    .forEach(eventListener -> {
                        if(eventListener instanceof AsyncChangeEquipListener) {
                            listenerChain.async(() -> ((AsyncChangeEquipListener) eventListener).onChangeEquip(oldReport, newReport, event, true));
                        } else{
                            listenerChain.sync(() -> ((SyncChangeEquipListener) eventListener).onChangeEquip(oldReport, newReport, event, true));
                        }
                    });
        });
        newEnchants.forEach((ench, e) -> {
            if(!ench.isEnabled() || !ench.isAllowedWorld(player.getWorld().getName())) return;
            ench.getEventListeners().stream()
                    .filter(eventListener -> eventListener instanceof SyncChangeEquipListener)
                    .forEach(eventListener -> {
                        if(eventListener instanceof AsyncChangeEquipListener) {
                            listenerChain.async(() -> ((AsyncChangeEquipListener) eventListener).onChangeEquip(oldReport, newReport, event, false));
                        } else{
                            listenerChain.sync(() -> ((SyncChangeEquipListener) eventListener).onChangeEquip(oldReport, newReport, event, false));
                        }
                    });
        });
        listenerChain.execute();
    }
}
