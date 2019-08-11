package dev.anhcraft.enc.listeners;

import co.aikar.taskchain.TaskChain;
import dev.anhcraft.craftkit.events.ArmorUnequipEvent;
import dev.anhcraft.enc.ENC;
import dev.anhcraft.enc.api.Enchantment;
import dev.anhcraft.enc.api.EnchantmentAPI;
import dev.anhcraft.enc.api.ItemReport;
import dev.anhcraft.enc.api.listeners.AsyncUnequipListener;
import dev.anhcraft.enc.api.listeners.SyncUnequipListener;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

public class UnequipListener implements Listener {
    @EventHandler
    public void unequip(ArmorUnequipEvent event){
        Player player = event.getPlayer();
        ItemStack item = event.getArmor();
        Map<Enchantment, Integer> enchants = EnchantmentAPI.listEnchantments(item);
        if(enchants.isEmpty()) return;
        ItemReport report = new ItemReport(player, item, enchants);
        TaskChain<Object> listenerChain = ENC.getTaskChainFactory().newChain();
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
