package dev.anhcraft.enc.listeners;

import co.aikar.taskchain.TaskChain;
import dev.anhcraft.enc.ENC;
import dev.anhcraft.enc.api.ActionReport;
import dev.anhcraft.enc.api.Enchantment;
import dev.anhcraft.enc.api.EnchantmentAPI;
import dev.anhcraft.enc.api.listeners.AsyncDeathDropListener;
import dev.anhcraft.enc.api.listeners.SyncDeathDropListener;
import org.anhcraft.keepmylife.events.PlayerKeepItemEvent;
import org.anhcraft.spaciouslib.builders.ArrayBuilder;
import org.anhcraft.spaciouslib.utils.InventoryUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class DeathDropListener {
    public static class KeepMyLife implements Listener {
        @EventHandler(priority = EventPriority.MONITOR)
        public void keep(PlayerKeepItemEvent event){
            Player owner = event.getPlayer();
            int i = 0;
            for(Iterator<ItemStack> it = event.getDropItems().iterator(); it.hasNext();) {
                ItemStack item = it.next();
                HashMap<Enchantment, Integer> enchants = EnchantmentAPI.listEnchantments(item);
                if(enchants.isEmpty()) {
                    continue;
                }
                ActionReport report = new ActionReport(owner, item, enchants, false);
                TaskChain<Boolean> listenerChain = ENC.getTaskChainFactory().newChain();
                for(Map.Entry<Enchantment, Integer> e : enchants.entrySet()) {
                    Enchantment enchantment = e.getKey();
                    if(!enchantment.isEnabled() || !enchantment.isAllowedWorld(owner.getWorld().getName())) {
                        continue;
                    }
                    enchantment.getEventListeners().stream()
                            .filter(eventListener -> eventListener instanceof SyncDeathDropListener)
                            .forEach(eventListener -> {
                                if(eventListener instanceof AsyncDeathDropListener) {
                                    listenerChain.async(() -> ((AsyncDeathDropListener) eventListener)
                                            .onDrop(report, item));
                                } else {
                                    listenerChain.sync(() -> ((SyncDeathDropListener) eventListener)
                                            .onDrop(report, item));
                                }
                            });
                }
                listenerChain.execute();
                if(report.isPrevented()){
                    event.getKeepItems().set(i, item);
                    it.remove();
                }
                i++;
            }
        }
    }

    public static class Default implements Listener {
        @EventHandler(priority = EventPriority.MONITOR)
        public void death(PlayerDeathEvent event){
            if(event.getKeepInventory()) {
                return;
            }
            Player owner = event.getEntity();
            Location location = owner.getLocation();
            ArrayBuilder keptItems = new ArrayBuilder(ItemStack.class);
            // we should use getInventory#getContents() instead of event#getDrops() to get correct item order
            for(ItemStack item : owner.getInventory().getContents()){
                if(InventoryUtils.isNull(item)){
                    keptItems.append(new ItemStack(Material.AIR, 1));
                    continue;
                }
                HashMap<Enchantment, Integer> enchants = EnchantmentAPI.listEnchantments(item);
                if(!enchants.isEmpty()){
                    ActionReport report = new ActionReport(owner, item, enchants, false);
                    TaskChain<Boolean> listenerChain = ENC.getTaskChainFactory().newChain();
                    for(Map.Entry<Enchantment, Integer> e : enchants.entrySet()) {
                        Enchantment enchantment = e.getKey();
                        if(enchantment.isEnabled() && enchantment.isAllowedWorld(owner.getWorld().getName())) {
                            enchantment.getEventListeners().stream()
                                .filter(eventListener -> eventListener instanceof SyncDeathDropListener)
                                .forEach(eventListener -> {
                                    if(eventListener instanceof AsyncDeathDropListener) {
                                        listenerChain.async(() -> ((AsyncDeathDropListener) eventListener)
                                                .onDrop(report, item));
                                    } else{
                                        listenerChain.sync(() -> ((SyncDeathDropListener) eventListener)
                                                .onDrop(report, item));
                                    }
                                });
                        }
                    }
                    listenerChain.execute();
                    if(report.isPrevented()){
                        keptItems.append(item);
                        continue;
                    }
                }
                location.getWorld().dropItemNaturally(location, item);
                keptItems.append(new ItemStack(Material.AIR, 1));
            }
            owner.getInventory().setContents((ItemStack[]) keptItems.build());
            owner.updateInventory();
            event.setKeepInventory(true);
        }
    }
}
