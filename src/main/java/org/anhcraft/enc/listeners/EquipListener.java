package org.anhcraft.enc.listeners;

import co.aikar.taskchain.TaskChain;
import org.anhcraft.enc.ENC;
import org.anhcraft.enc.api.ActionReport;
import org.anhcraft.enc.api.Enchantment;
import org.anhcraft.enc.api.EnchantmentAPI;
import org.anhcraft.enc.api.listeners.AsyncEquipListener;
import org.anhcraft.enc.api.listeners.SyncEquipListener;
import org.anhcraft.spaciouslib.events.ArmorEquipEvent;
import org.anhcraft.spaciouslib.inventory.EquipSlot;
import org.anhcraft.spaciouslib.utils.InventoryUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class EquipListener implements Listener {
    @EventHandler(ignoreCancelled = true)
    public void equip(ArmorEquipEvent event){
        Player player = event.getPlayer();
        ItemStack oldArmor = event.getOldArmor();
        ItemStack newArmor = event.getNewArmor();
        EquipSlot slot = event.getSlot();
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
                        .filter(eventListener -> eventListener instanceof SyncEquipListener)
                        .forEach(eventListener -> {
                            if(eventListener instanceof AsyncEquipListener) {
                                listenerChain.async(() -> ((AsyncEquipListener) eventListener)
                                        .onEquip(report, oldArmor, newArmor, slot));
                            } else{
                                listenerChain.sync(() -> ((SyncEquipListener) eventListener)
                                        .onEquip(report, oldArmor, newArmor, slot));
                            }
                        });
            }
            listenerChain.execute();
        }
    }
}
