package dev.anhcraft.enc.listeners;

import dev.anhcraft.craftkit.events.PlayerJumpEvent;
import dev.anhcraft.craftkit.utils.ItemUtil;
import dev.anhcraft.enc.ENC;
import dev.anhcraft.enc.api.EnchantmentAPI;
import dev.anhcraft.enc.api.ItemReport;
import dev.anhcraft.enc.api.listeners.AsyncJumpListener;
import dev.anhcraft.enc.api.listeners.SyncJumpListener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class JumpListener implements Listener {
    @EventHandler
    public void jump(PlayerJumpEvent event){
        var player = event.getPlayer();
        var item = player.getInventory().getBoots();
        if(ItemUtil.isNull(item)) return;
        var enchants = EnchantmentAPI.listEnchantments(item);
        if(enchants.isEmpty()) return;
        var report = new ItemReport(player, item, enchants);
        var listenerChain = ENC.getTaskChainFactory().newChain();
        enchants.forEach((ench, value) -> {
            if(!ench.isEnabled() || !ench.isAllowedWorld(player.getWorld().getName())) return;
            ench.getEventListeners().stream()
                    .filter(eventListener -> eventListener instanceof SyncJumpListener)
                    .forEach(eventListener -> {
                        if(eventListener instanceof AsyncJumpListener) {
                            listenerChain.async(() -> ((AsyncJumpListener) eventListener)
                                    .onJump(report, event));
                        } else{
                            listenerChain.sync(() -> ((SyncJumpListener) eventListener)
                                    .onJump(report, event));
                        }
                    });
        });
        listenerChain.execute();
    }
}
