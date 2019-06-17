package dev.anhcraft.enc.listeners;

import dev.anhcraft.craftkit.utils.ItemUtil;
import dev.anhcraft.enc.ENC;
import dev.anhcraft.enc.api.EnchantmentAPI;
import dev.anhcraft.enc.api.ItemReport;
import dev.anhcraft.enc.api.listeners.AsyncBreakBlockListener;
import dev.anhcraft.enc.api.listeners.SyncBreakBlockListener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

public class BlockBreakListener implements Listener {
    @EventHandler
    public void blockBreak(BlockBreakEvent event){
        var player = event.getPlayer();
        var item = player.getInventory().getItemInMainHand();
        if(ItemUtil.isNull(item)) return;
        var enchants = EnchantmentAPI.listEnchantments(item);
        if(enchants.isEmpty()) return;
        var report = new ItemReport(player, item, enchants);
        var listenerChain = ENC.getTaskChainFactory().newChain();
        enchants.forEach((ench, value) -> {
            if(!ench.isEnabled() || !ench.isAllowedWorld(player.getWorld().getName())) return;
            ench.getEventListeners().stream()
                    .filter(eventListener -> eventListener instanceof SyncBreakBlockListener)
                    .forEach(eventListener -> {
                        if(eventListener instanceof AsyncBreakBlockListener) {
                            listenerChain.async(() -> ((AsyncBreakBlockListener) eventListener)
                                    .onBreakBlock(report, event));
                        } else{
                            listenerChain.sync(() -> ((SyncBreakBlockListener) eventListener)
                                    .onBreakBlock(report, event));
                        }
                    });
        });
        listenerChain.execute();
    }
}
