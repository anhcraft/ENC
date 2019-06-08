package dev.anhcraft.enc.listeners;

import dev.anhcraft.craftkit.events.PlayerJumpEvent;
import dev.anhcraft.craftkit.utils.ItemUtil;
import dev.anhcraft.enc.ENC;
import dev.anhcraft.enc.api.ActionReport;
import dev.anhcraft.enc.api.EnchantmentAPI;
import dev.anhcraft.enc.api.listeners.AsyncJumpListener;
import dev.anhcraft.enc.api.listeners.SyncJumpListener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class JumpListener implements Listener {
    @EventHandler
    public void jump(PlayerJumpEvent event){
        var player = event.getPlayer();
        var item = player.getInventory().getBoots();
        if(!ItemUtil.isNull(item)) {
            var enchants = EnchantmentAPI.listEnchantments(item);
            if(enchants.isEmpty()) return;
            var report = new ActionReport(player, item, enchants, false);
            var listenerChain = ENC.getTaskChainFactory().newChain();
            for(var e : enchants.entrySet()) {
                var enchantment = e.getKey();
                if(!enchantment.isEnabled() || !enchantment.isAllowedWorld(player.getWorld().getName())) continue;
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
