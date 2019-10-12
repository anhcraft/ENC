package dev.anhcraft.enc.listeners;

import dev.anhcraft.craftkit.events.ArmorUnequipEvent;
import dev.anhcraft.enc.api.Enchantment;
import dev.anhcraft.enc.api.EnchantmentAPI;
import dev.anhcraft.enc.api.ItemReport;
import dev.anhcraft.enc.api.handlers.UnequipHandler;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

public class UnequipListener implements Listener {
    @EventHandler
    public void unequip(ArmorUnequipEvent event){
        Player player = event.getPlayer();
        ItemStack item = event.getArmor();
        Map<Enchantment, Integer> enchants = EnchantmentAPI.listEnchantments(item);
        if(enchants.isEmpty()) return;
        ItemReport report = new ItemReport(player, item, enchants);
        enchants.forEach((ench, value) -> {
            if(!ench.isEnabled() || !ench.isAllowedWorld(player.getWorld().getName())) return;
            ench.getEnchantHandlers().stream()
                    .filter(handler -> handler instanceof UnequipHandler)
                    .forEach(handler -> {
                        ((UnequipHandler) handler).onUnequip(report, event);
                    });
        });
    }
}
