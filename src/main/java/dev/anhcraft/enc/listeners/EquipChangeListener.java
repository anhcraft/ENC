package dev.anhcraft.enc.listeners;

import co.aikar.taskchain.TaskChain;
import dev.anhcraft.craftkit.events.ArmorChangeEvent;
import dev.anhcraft.enc.ENC;
import dev.anhcraft.enc.api.Enchantment;
import dev.anhcraft.enc.api.EnchantmentAPI;
import dev.anhcraft.enc.api.ItemReport;
import dev.anhcraft.enc.api.listeners.AsyncChangeEquipListener;
import dev.anhcraft.enc.api.listeners.SyncChangeEquipListener;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

public class EquipChangeListener implements Listener {
    @EventHandler
    public void changeEquip(ArmorChangeEvent event){
        Player player = event.getPlayer();
        ItemStack oldItem = event.getOldArmor();
        Map<Enchantment, Integer> oldEnchants = EnchantmentAPI.listEnchantments(oldItem);
        ItemStack newItem = event.getNewArmor();
        Map<Enchantment, Integer> newEnchants = EnchantmentAPI.listEnchantments(newItem);
        TaskChain<Object> listenerChain = ENC.getTaskChainFactory().newChain();
        ItemReport oldReport = new ItemReport(player, oldItem, oldEnchants);
        ItemReport newReport = new ItemReport(player, newItem, newEnchants);
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
