package dev.anhcraft.enc.listeners;

import dev.anhcraft.craftkit.utils.ItemUtil;
import dev.anhcraft.enc.api.Enchantment;
import dev.anhcraft.enc.api.EnchantmentAPI;
import dev.anhcraft.enc.api.ItemReport;
import dev.anhcraft.enc.api.handlers.KillHandler;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

public class KillListener implements Listener {
    @EventHandler
    public void death(EntityDeathEvent event){
        Player killer = event.getEntity().getKiller();
        if(killer == null) return;
        ItemStack item = killer.getInventory().getItemInMainHand();
        if(ItemUtil.isNull(item)) return;
        Map<Enchantment, Integer> enchants = EnchantmentAPI.listEnchantments(item);
        if(enchants.isEmpty()) return;
        ItemReport report = new ItemReport(killer, item, enchants);
        enchants.forEach((ench, value) -> {
            if(!ench.isEnabled() || !ench.isAllowedWorld(killer.getWorld().getName())) return;
            ench.getEnchantHandlers().stream()
                    .filter(handler -> handler instanceof KillHandler)
                    .forEach(handler -> {
                        ((KillHandler) handler).onKill(report, event);
                    });
        });
    }
}
