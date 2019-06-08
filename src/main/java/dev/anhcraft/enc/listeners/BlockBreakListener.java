package dev.anhcraft.enc.listeners;

import co.aikar.taskchain.TaskChain;
import dev.anhcraft.craftkit.utils.ItemUtil;
import dev.anhcraft.enc.ENC;
import dev.anhcraft.enc.api.ActionReport;
import dev.anhcraft.enc.api.EnchantmentAPI;
import dev.anhcraft.enc.api.listeners.AsyncBlockBreakListener;
import dev.anhcraft.enc.api.listeners.SyncBlockBreakListener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

public class BlockBreakListener implements Listener {
    @EventHandler(ignoreCancelled = true)
    public void blockBreak(BlockBreakEvent event){
        var player = event.getPlayer();
        var block = event.getBlock();
        var item = player.getInventory().getItemInMainHand();
        if(!ItemUtil.isNull(item)) {
            var enchants = EnchantmentAPI.listEnchantments(item);
            if(enchants.isEmpty()) return;
            var report = new ActionReport(player, item, enchants, false);
            TaskChain<Boolean> listenerChain = ENC.getTaskChainFactory().newChain();
            for(var e : enchants.entrySet()) {
                var enchantment = e.getKey();
                if(!enchantment.isEnabled() || !enchantment.isAllowedWorld(player.getWorld().getName())) continue;
                enchantment.getEventListeners().stream()
                        .filter(eventListener -> eventListener instanceof SyncBlockBreakListener)
                        .forEach(eventListener -> {
                            if(eventListener instanceof AsyncBlockBreakListener) {
                                listenerChain.async(() -> ((AsyncBlockBreakListener) eventListener)
                                        .onBreakBlock(report, block));
                            } else{
                                listenerChain.sync(() -> ((SyncBlockBreakListener) eventListener)
                                        .onBreakBlock(report, block));
                            }
                        });
            }
            listenerChain.execute();
            event.setCancelled(report.isPrevented());
        }
    }
}