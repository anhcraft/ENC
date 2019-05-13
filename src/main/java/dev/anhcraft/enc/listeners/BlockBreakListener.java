package dev.anhcraft.enc.listeners;

import co.aikar.taskchain.TaskChain;
import dev.anhcraft.enc.ENC;
import dev.anhcraft.enc.api.ActionReport;
import dev.anhcraft.enc.api.Enchantment;
import dev.anhcraft.enc.api.EnchantmentAPI;
import dev.anhcraft.enc.api.listeners.AsyncBlockBreakListener;
import dev.anhcraft.enc.api.listeners.SyncBlockBreakListener;
import org.anhcraft.spaciouslib.utils.InventoryUtils;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class BlockBreakListener implements Listener {
    @EventHandler(ignoreCancelled = true)
    public void blockBreak(BlockBreakEvent event){
        Player player = event.getPlayer();
        Block block = event.getBlock();
        ItemStack item = player.getInventory().getItemInMainHand();
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
