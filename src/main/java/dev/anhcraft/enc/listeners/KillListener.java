package dev.anhcraft.enc.listeners;

import dev.anhcraft.craftkit.utils.ItemUtil;
import dev.anhcraft.enc.ENC;
import dev.anhcraft.enc.api.EnchantmentAPI;
import dev.anhcraft.enc.api.ItemReport;
import dev.anhcraft.enc.api.listeners.AsyncKillListener;
import dev.anhcraft.enc.api.listeners.SyncKillListener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;

public class KillListener implements Listener {
    @EventHandler
    public void death(EntityDeathEvent event){
        var killer = event.getEntity().getKiller();
        if(killer == null) return;
        var item = killer.getInventory().getItemInMainHand();
        if(ItemUtil.isNull(item)) return;
        var enchants = EnchantmentAPI.listEnchantments(item);
        if(enchants.isEmpty()) return;
        var report = new ItemReport(killer, item, enchants);
        var listenerChain = ENC.getTaskChainFactory().newChain();
        enchants.forEach((ench, value) -> {
            if(!ench.isEnabled() || !ench.isAllowedWorld(killer.getWorld().getName())) return;
            ench.getEventListeners().stream()
                    .filter(eventListener -> eventListener instanceof SyncKillListener)
                    .forEach(eventListener -> {
                        if(eventListener instanceof AsyncKillListener) {
                            listenerChain.async(() -> ((AsyncKillListener) eventListener)
                                    .onKill(report, event));
                        } else{
                            listenerChain.sync(() -> ((SyncKillListener) eventListener)
                                    .onKill(report, event));
                        }
                    });
        });
        listenerChain.execute();
    }
}
