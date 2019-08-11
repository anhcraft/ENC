package dev.anhcraft.enc.listeners;

import co.aikar.taskchain.TaskChain;
import dev.anhcraft.craftkit.utils.ItemUtil;
import dev.anhcraft.enc.ENC;
import dev.anhcraft.enc.api.Enchantment;
import dev.anhcraft.enc.api.EnchantmentAPI;
import dev.anhcraft.enc.api.ItemReport;
import dev.anhcraft.enc.api.listeners.AsyncBreakBlockListener;
import dev.anhcraft.enc.api.listeners.SyncBreakBlockListener;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

public class BlockBreakListener implements Listener {
    @EventHandler
    public void blockBreak(BlockBreakEvent event){
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();
        if(ItemUtil.isNull(item)) return;
        Map<Enchantment, Integer> enchants = EnchantmentAPI.listEnchantments(item);
        if(enchants.isEmpty()) return;
        ItemReport report = new ItemReport(player, item, enchants);
        TaskChain<Object> listenerChain = ENC.getTaskChainFactory().newChain();
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
