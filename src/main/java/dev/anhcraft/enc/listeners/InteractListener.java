package dev.anhcraft.enc.listeners;

import co.aikar.taskchain.TaskChain;
import dev.anhcraft.craftkit.utils.ItemUtil;
import dev.anhcraft.enc.ENC;
import dev.anhcraft.enc.api.Enchantment;
import dev.anhcraft.enc.api.EnchantmentAPI;
import dev.anhcraft.enc.api.ItemReport;
import dev.anhcraft.enc.api.listeners.AsyncInteractListener;
import dev.anhcraft.enc.api.listeners.SyncInteractListener;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

public class InteractListener implements Listener {
    @EventHandler
    public void interact(PlayerInteractEvent event){
        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        if(ItemUtil.isNull(item)) return;
        Map<Enchantment, Integer> enchants = EnchantmentAPI.listEnchantments(item);
        if(enchants.isEmpty()) return;
        ItemReport report = new ItemReport(player, item, enchants);
        TaskChain<Object> listenerChain = ENC.getTaskChainFactory().newChain();
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
