package org.anhcraft.enc.listeners;

import co.aikar.taskchain.TaskChain;
import org.anhcraft.enc.ENC;
import org.anhcraft.enc.api.ActionReport;
import org.anhcraft.enc.api.Enchantment;
import org.anhcraft.enc.api.EnchantmentAPI;
import org.anhcraft.enc.api.listeners.AsyncJumpListener;
import org.anhcraft.enc.api.listeners.SyncJumpListener;
import org.anhcraft.spaciouslib.events.PlayerJumpEvent;
import org.anhcraft.spaciouslib.utils.InventoryUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class JumpListener implements Listener {
    @EventHandler
    public void jump(PlayerJumpEvent event){
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getBoots();
        if(!InventoryUtils.isNull(item)) {
            HashMap<Enchantment, Integer> enchants = EnchantmentAPI.listEnchantments(item);
            if(enchants.isEmpty()){
                return;
            }
            ActionReport report = new ActionReport(player, item, enchants, false);
            TaskChain<Boolean> listenerChain = ENC.getTaskChainFactory().newChain();
            for(Map.Entry<Enchantment, Integer> e : enchants.entrySet()) {
                Enchantment enchantment = e.getKey();
                if(!enchantment.isEnabled() ||
                        !enchantment.isAllowedWorld(player.getWorld().getName())) {
                    continue;
                }
                enchantment.getEventListeners().stream()
                        .filter(eventListener -> eventListener instanceof SyncJumpListener)
                        .forEach(eventListener -> {
                            if(eventListener instanceof AsyncJumpListener) {
                                listenerChain.async(() -> ((AsyncJumpListener) eventListener)
                                        .onJump(report, event.isOnSpot()));
                            } else{
                                listenerChain.sync(() -> ((SyncJumpListener) eventListener)
                                        .onJump(report, event.isOnSpot()));
                            }
                        });
            }
            listenerChain.execute();
        }
    }
}
