package dev.anhcraft.enc.listeners;

import co.aikar.taskchain.TaskChain;
import dev.anhcraft.craftkit.utils.ItemUtil;
import dev.anhcraft.enc.ENC;
import dev.anhcraft.enc.api.ActionReport;
import dev.anhcraft.enc.api.EnchantmentAPI;
import dev.anhcraft.enc.api.listeners.AsyncDeathDropListener;
import dev.anhcraft.enc.api.listeners.SyncDeathDropListener;
import dev.anhcraft.jvmkit.utils.CollectionUtil;
import dev.anhcraft.keepmylife.api.events.KeepItemEvent;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;

import java.util.LinkedList;
import java.util.List;

public class DeathDropListener {
    public static class KeepMyLife implements Listener {
        @EventHandler
        public void keep(KeepItemEvent event){
            var owner = event.getPlayer();
            var i = 0;
            for(var it = event.getDropItems().iterator(); it.hasNext();) {
                var item = it.next();
                var enchants = EnchantmentAPI.listEnchantments(item);
                if(enchants.isEmpty()) continue;
                var report = new ActionReport(owner, item, enchants, false);
                TaskChain<Boolean> listenerChain = ENC.getTaskChainFactory().newChain();
                for(var e : enchants.entrySet()) {
                    var enchantment = e.getKey();
                    if(!enchantment.isEnabled() || !enchantment.isAllowedWorld(owner.getWorld().getName())) continue;
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
        @EventHandler
        public void death(PlayerDeathEvent event){
            if(event.getKeepInventory()) return;
            var owner = event.getEntity();
            var location = owner.getLocation();
            // use linked list to keep the item order
            List<ItemStack> keptItems = new LinkedList<>();
            for(var item : owner.getInventory().getContents()){
                if(ItemUtil.isNull(item)){
                    keptItems.add(new ItemStack(Material.AIR, 1));
                    continue;
                }
                var enchants = EnchantmentAPI.listEnchantments(item);
                if(!enchants.isEmpty()){
                    var report = new ActionReport(owner, item, enchants, false);
                    TaskChain<Boolean> listenerChain = ENC.getTaskChainFactory().newChain();
                    for(var e : enchants.entrySet()) {
                        var enchantment = e.getKey();
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
                        keptItems.add(item);
                        continue;
                    }
                }
                location.getWorld().dropItemNaturally(location, item);
                keptItems.add(new ItemStack(Material.AIR, 1));
            }
            owner.getInventory().setContents(CollectionUtil.toArray(keptItems, ItemStack.class));
            owner.updateInventory();
            event.setKeepInventory(true);
        }
    }
}
