package dev.anhcraft.enc.enchant;

import dev.anhcraft.enc.ENC;
import dev.anhcraft.enc.api.Enchantment;
import dev.anhcraft.enc.api.ItemReport;
import dev.anhcraft.enc.api.handlers.InteractHandler;
import dev.anhcraft.enc.utils.Cooldown;
import dev.anhcraft.enc.utils.EntityFilter;
import dev.anhcraft.enc.utils.PlayerMap;
import org.bukkit.Location;
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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class Spray extends Enchantment {
    private final PlayerMap<Cooldown> MAP = new PlayerMap<>();

    public Spray() {
        super("Spray", new String[]{
            "Spray forward and impede targets with negative effects"
        }, "anhcraft", null, 5, EnchantmentTarget.ALL);

        getEnchantHandlers().add(new InteractHandler() {
            @Override
            public void onInteract(ItemReport report, PlayerInteractEvent event) {
                Player player = report.getPlayer();
                if(Objects.equals(EquipmentSlot.OFF_HAND, event.getHand()) ||
                        (event.getAction() != Action.RIGHT_CLICK_BLOCK
                                && event.getAction() != Action.RIGHT_CLICK_AIR)) return;

                double cooldown = computeConfigValue("cooldown", report);
                if(!handleCooldown(MAP, player, cooldown)) return;

                int level = (int) computeConfigValue("effect_level", report);
                int duration = (int) computeConfigValue("effect_duration", report);
                double damage = computeConfigValue("damage", report);
                double distance = computeConfigValue("distance", report);
                Location loc = player.getEyeLocation();

                for (int g = 1; g < distance; g++) {
                    Location target = loc.clone().add(loc.getDirection().normalize().multiply(g));
                    ENC.getEffectManager().display(Particle.WATER_DROP, target, 0, 0, 0, 0, 6, 3, null, null, (byte) 0, 50, target.getWorld().getPlayers());
                    Collection<Entity> entities = player.getWorld().getNearbyEntities(target, 3, 3, 3);
                    ENC.getTaskChainFactory().newChain().sync(() -> {
                        for (Entity ent : entities) {
                            if (ent.equals(player) || !EntityFilter.check(ent)) continue;
                            LivingEntity le = (LivingEntity) ent;
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
