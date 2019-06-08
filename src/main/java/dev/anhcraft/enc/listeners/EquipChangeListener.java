package dev.anhcraft.enc.listeners;

import dev.anhcraft.craftkit.events.ArmorChangeEvent;
import dev.anhcraft.craftkit.utils.ItemUtil;
import dev.anhcraft.enc.ENC;
import dev.anhcraft.enc.api.ActionReport;
import dev.anhcraft.enc.api.EnchantmentAPI;
import dev.anhcraft.enc.api.listeners.AsyncEquipChangeListener;
import dev.anhcraft.enc.api.listeners.SyncEquipChangeListener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class EquipChangeListener implements Listener {
    @EventHandler(ignoreCancelled = true)
    public void changeEquip(ArmorChangeEvent event){
        var player = event.getPlayer();
        var oldArmor = event.getOldArmor();
        var newArmor = event.getNewArmor();
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
                        .filter(eventListener -> eventListener instanceof SyncEquipChangeListener)
                        .forEach(eventListener -> {
                            if(eventListener instanceof AsyncEquipChangeListener) {
                                listenerChain.async(() -> ((AsyncEquipChangeListener) eventListener)
                                        .onEquip(report, oldArmor, newArmor, slot));
                            } else{
                                listenerChain.sync(() -> ((SyncEquipChangeListener) eventListener)
                                        .onEquip(report, oldArmor, newArmor, slot));
                            }
                        });
            }
            listenerChain.execute();
        }
    }
}
