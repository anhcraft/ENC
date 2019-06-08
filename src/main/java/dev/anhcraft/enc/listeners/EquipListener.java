package dev.anhcraft.enc.listeners;

import dev.anhcraft.craftkit.events.ArmorEquipEvent;
import dev.anhcraft.craftkit.utils.ItemUtil;
import dev.anhcraft.enc.ENC;
import dev.anhcraft.enc.api.ActionReport;
import dev.anhcraft.enc.api.EnchantmentAPI;
import dev.anhcraft.enc.api.listeners.AsyncEquipListener;
import dev.anhcraft.enc.api.listeners.SyncEquipListener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class EquipListener implements Listener {
    @EventHandler(ignoreCancelled = true)
    public void equip(ArmorEquipEvent event){
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
                if(!enchantment.isEnabled() || !enchantment.isAllowedWorld(player.getWorld().getName())) continue;
                enchantment.getEventListeners().stream()
                        .filter(eventListener -> eventListener instanceof SyncEquipListener)
                        .forEach(eventListener -> {
                            if(eventListener instanceof AsyncEquipListener) {
                                listenerChain.async(() -> ((AsyncEquipListener) eventListener)
                                        .onEquip(report, armor, slot));
                            } else{
                                listenerChain.sync(() -> ((SyncEquipListener) eventListener)
                                        .onEquip(report, armor, slot));
                            }
                        });
            }
            listenerChain.execute();
        }
    }
}
