package dev.anhcraft.enc.listeners;

import dev.anhcraft.craftkit.utils.ItemUtil;
import dev.anhcraft.enc.api.Enchantment;
import dev.anhcraft.enc.api.EnchantmentAPI;
import dev.anhcraft.enc.api.ItemReport;
import dev.anhcraft.enc.api.handlers.AttackHandler;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

public class AttackListener implements Listener {
    @EventHandler
    public void attack(EntityDamageByEntityEvent event){
        if(event.getDamager() instanceof Player && event.getEntity() instanceof LivingEntity){
            Player damager = (Player) event.getDamager();
            ItemStack item = damager.getInventory().getItemInMainHand();
            if(ItemUtil.isNull(item)) return;
            Map<Enchantment, Integer> enchants = EnchantmentAPI.listEnchantments(item);
            if(enchants.isEmpty()) return;
            LivingEntity entity = (LivingEntity) event.getEntity();
            ItemReport report = new ItemReport(damager, item, enchants);
            enchants.forEach((ench, value) -> {
                if(!ench.isEnabled() || !ench.isAllowedWorld(damager.getWorld().getName())) return;
                ench.getEnchantHandlers().stream()
                        .filter(handler -> handler instanceof AttackHandler)
                        .forEach(handler -> {
                            ((AttackHandler) handler).onAttack(report, event, entity);
                        });
            });
        }
    }
}
