package dev.anhcraft.enc.listeners;

import dev.anhcraft.craftkit.events.PlayerJumpEvent;
import dev.anhcraft.craftkit.utils.ItemUtil;
import dev.anhcraft.enc.api.Enchantment;
import dev.anhcraft.enc.api.EnchantmentAPI;
import dev.anhcraft.enc.api.ItemReport;
import dev.anhcraft.enc.api.handlers.JumpHandler;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

public class JumpListener implements Listener {
    @EventHandler
    public void jump(PlayerJumpEvent event){
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getBoots();
        if(ItemUtil.isNull(item)) return;
        Map<Enchantment, Integer> enchants = EnchantmentAPI.listEnchantments(item);
        if(enchants.isEmpty()) return;
        ItemReport report = new ItemReport(player, item, enchants);
        enchants.forEach((ench, value) -> {
            if(!ench.isEnabled() || !ench.isAllowedWorld(player.getWorld().getName())) return;
            ench.getEnchantHandlers().stream()
                    .filter(handler -> handler instanceof JumpHandler)
                    .forEach(handler -> {
                        ((JumpHandler) handler).onJump(report, event);
                    });
        });
    }
}
