package dev.anhcraft.enc.listeners;

import co.aikar.taskchain.TaskChain;
import dev.anhcraft.craftkit.utils.ItemUtil;
import dev.anhcraft.enc.ENC;
import dev.anhcraft.enc.api.Enchantment;
import dev.anhcraft.enc.api.EnchantmentAPI;
import dev.anhcraft.enc.api.ItemReport;
import dev.anhcraft.enc.api.listeners.AsyncDeathDropListener;
import dev.anhcraft.enc.api.listeners.SyncDeathDropListener;
import dev.anhcraft.jvmkit.utils.CollectionUtil;
import dev.anhcraft.keepmylife.api.events.KeepItemEvent;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class DeathDropListener {
    public static class KeepMyLife implements Listener {
        @EventHandler
        public void keep(KeepItemEvent event){
            Player owner = event.getPlayer();
            int i = 0;
            for(Iterator<ItemStack> it = event.getDropItems().iterator(); it.hasNext();) {
                ItemStack item = it.next();
                Map<Enchantment, Integer> enchants = EnchantmentAPI.listEnchantments(item);
                if(enchants.isEmpty()) continue;
                ItemReport report = new ItemReport(owner, item, enchants);
                AtomicBoolean keep = new AtomicBoolean(false);
                TaskChain<Object> listenerChain = ENC.getTaskChainFactory().newChain();
                enchants.forEach((ench, value) -> {
                    if (!ench.isEnabled() || !ench.isAllowedWorld(owner.getWorld().getName())) return;
                    ench.getEventListeners().stream()
                            .filter(eventListener -> eventListener instanceof SyncDeathDropListener)
                            .forEach(eventListener -> {
                                if(eventListener instanceof AsyncDeathDropListener) {
                                    listenerChain.async(() -> ((AsyncDeathDropListener) eventListener)
                                            .onDrop(report, keep));
                                } else {
                                    listenerChain.sync(() -> ((SyncDeathDropListener) eventListener)
                                            .onDrop(report, keep));
                                }
                            });
                });
                listenerChain.execute();
                if(keep.get()){
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
            Player owner = event.getEntity();
            Location location = owner.getLocation();
            // use linked list to keep the item order
            List<ItemStack> keptItems = new LinkedList<>();
            for(ItemStack item : owner.getInventory().getContents()){
                if(ItemUtil.isNull(item)){
                    keptItems.add(new ItemStack(Material.AIR, 1));
                    continue;
                }
                Map<Enchantment, Integer> enchants = EnchantmentAPI.listEnchantments(item);
                if(!enchants.isEmpty()){
                    ItemReport report = new ItemReport(owner, item, enchants);
                    AtomicBoolean keep = new AtomicBoolean(false);
                    TaskChain<Object> listenerChain = ENC.getTaskChainFactory().newChain();
                    enchants.forEach((ench, value) -> {
                        if (!ench.isEnabled() || !ench.isAllowedWorld(owner.getWorld().getName())) return;
                        ench.getEventListeners().stream()
                                .filter(eventListener -> eventListener instanceof SyncDeathDropListener)
                                .forEach(eventListener -> {
                                    if(eventListener instanceof AsyncDeathDropListener) {
                                        listenerChain.async(() -> ((AsyncDeathDropListener) eventListener)
                                                .onDrop(report, keep));
                                    } else {
                                        listenerChain.sync(() -> ((SyncDeathDropListener) eventListener)
                                                .onDrop(report, keep));
                                    }
                                });
                    });
                    listenerChain.execute();
                    if(keep.get()){
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
