package dev.anhcraft.enc.enchantments;

import dev.anhcraft.craftkit.common.lang.annotation.RequiredCleaner;
import dev.anhcraft.enc.ENC;
import dev.anhcraft.enc.api.Enchantment;
import dev.anhcraft.enc.api.ItemReport;
import dev.anhcraft.enc.api.listeners.AsyncInteractListener;
import dev.anhcraft.enc.utils.Cooldown;
import dev.anhcraft.enc.utils.FilterAssistant;
import org.bukkit.Particle;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class Spray extends Enchantment {
    @RequiredCleaner
    private static final Map<Player, Cooldown> MAP = new HashMap<>();

    public Spray() {
        super("Spray", new String[]{
            "Spray forward and impede targets with negative effects"
        }, "anhcraft", null, 5, EnchantmentTarget.ALL);

        getEventListeners().add(new AsyncInteractListener() {
            @Override
            public void onInteract(ItemReport report, PlayerInteractEvent event) {
                var player = report.getPlayer();
                var loc = player.getEyeLocation();
                if(Objects.equals(EquipmentSlot.OFF_HAND, event.getHand()) ||
                        (event.getAction() != Action.RIGHT_CLICK_BLOCK
                                && event.getAction() != Action.RIGHT_CLICK_AIR)) return;

                var level = (int) computeConfigValue("effect_level", report);
                var duration = (int) computeConfigValue("effect_duration", report);
                var cooldown = computeConfigValue("cooldown", report);
                var damage = computeConfigValue("damage", report);
                var distance = computeConfigValue("distance", report);

                if(!handleCooldown(MAP, player, cooldown)) return;

                for (var g = 1; g < distance; g++) {
                    var target = loc.clone().add(loc.getDirection().normalize().multiply(g));
                    ENC.getEffectManager().display(Particle.WATER_DROP, target, 0, 0, 0, 0, 12, 3, null, null, (byte) 0, 50, target.getWorld().getPlayers());
                    var entities = player.getWorld().getNearbyEntities(target, 3.5, 3.5, 3.5);
                    ENC.getTaskChainFactory().newChain().sync(() -> {
                        for (var ent : entities) {
                            if (!(ent instanceof LivingEntity) ||
                                    FilterAssistant.anyMatch(player, Entity.class))
                                continue;
                            var le = (LivingEntity) ent;
                            le.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, duration, level));
                            le.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, duration, level));
                            le.damage(damage, player);
                        }
                    }).execute();
                }
            }
        });
    }

    @Override
    public void onInitConfig(){
        Map<String, Object> map = new HashMap<>();
        map.put("cooldown", "{level}*10+20");
        map.put("effect_level", "ceil({level}*0.75)");
        map.put("effect_duration", "{level}*30+40");
        map.put("damage", "{level}*0.3+0.6");
        map.put("distance", "12+{level}*1.5");
        initConfigEntries(map);
    }
}
