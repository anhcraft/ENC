package dev.anhcraft.enc.listeners;

import dev.anhcraft.craftkit.events.ArmorUnequipEvent;
import dev.anhcraft.craftkit.utils.ItemUtil;
import dev.anhcraft.enc.ENC;
import dev.anhcraft.enc.api.ActionReport;
import dev.anhcraft.enc.api.EnchantmentAPI;
import dev.anhcraft.enc.api.listeners.AsyncUnquipListener;
import dev.anhcraft.enc.api.listeners.SyncUnquipListener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class UnequipListener implements Listener {
    @EventHandler(ignoreCancelled = true)
    public void unequip(ArmorUnequipEvent event){
        var player = event.getPlayer();
        var armor = event.getArmor();
        var slot = event.getSlot();
        var item = player.getInventory().getItemInMainHand();
        if(!ItemUtil.isNull(item)) {
            var enchants = EnchantmentAPI.listEnchantments(item);
            if(enchants.isEmpty()) return;
            var report = new ActionReport(player, item, enchants, false);
            var listenerChain = ENC.getTaskChainFactory().newChain();
            for(var e : enchants.entrySet()) {
                var enchantment = e.getKey();
                if(!enchantment.isEnabled() ||
                        !enchantment.isAllowedWorld(player.getWorld().getName())) continue;
                enchantment.getEventListeners().stream()
                        .filter(eventListener -> eventListener instanceof SyncUnquipListener)
                        .forEach(eventListener -> {
                            if(eventListener instanceof AsyncUnquipListener) {
                                listenerChain.async(() -> ((AsyncUnquipListener) eventListener)
                                        .onUnequip(report, armor, slot));
                            } else{
                                listenerChain.sync(() -> ((SyncUnquipListener) eventListener)
                                        .onUnequip(report, armor, slot));
                            }
                        });
            }
            listenerChain.execute();
        }
    }
}
