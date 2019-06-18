package dev.anhcraft.enc.enchantments;

import dev.anhcraft.craftkit.cb_common.lang.enumeration.NMSVersion;
import dev.anhcraft.craftkit.common.lang.annotation.RequiredCleaner;
import dev.anhcraft.enc.ENC;
import dev.anhcraft.enc.api.Enchantment;
import dev.anhcraft.enc.api.ItemReport;
import dev.anhcraft.enc.api.listeners.AsyncInteractListener;
import dev.anhcraft.enc.utils.Cooldown;
import dev.anhcraft.enc.utils.EntityFilter;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class Pull extends Enchantment {
    private static final Sound SOUND = NMSVersion.getNMSVersion().isNewerOrSame(NMSVersion.v1_13_R1) ? Sound.valueOf("BLOCK_SLIME_BLOCK_HIT") : Sound.BLOCK_SLIME_HIT;
    @RequiredCleaner
    private static final Map<Player, Cooldown> MAP = new HashMap<>();

    public Pull() {
        super("Pull", new String[]{
            "Pulls entities toward you"
        }, "anhcraft", null, 3, EnchantmentTarget.ALL);

        getEventListeners().add(new AsyncInteractListener() {
            @Override
            public void onInteract(ItemReport report, PlayerInteractEvent event) {
                var player = report.getPlayer();
                var loc = player.getEyeLocation();
                if(Objects.equals(EquipmentSlot.OFF_HAND, event.getHand()) ||
                        (event.getAction() != Action.RIGHT_CLICK_BLOCK
                                && event.getAction() != Action.RIGHT_CLICK_AIR)) return;

                var amount = (int) computeConfigValue("amount", report);
                var cooldown = computeConfigValue("cooldown", report);
                var distance = computeConfigValue("distance", report);
                var damage = computeConfigValue("damage", report);

                if(!handleCooldown(MAP, player, cooldown)) return;

                player.playSound(player.getLocation(), SOUND, 3f, 1f);
                for (var g = 1; g < distance; g++) {
                    var target = loc.clone().add(loc.getDirection().normalize().multiply(g));
                    ENC.getEffectManager().display(Particle.VILLAGER_HAPPY, target, 0, 0, 0, 0, 10, 3, null, null, (byte) 0, 50, target.getWorld().getPlayers());
                    var entities = player.getWorld().getNearbyEntities(target, 1.5, 1.5, 1.5)
                            .stream().limit(amount);
                    ENC.getTaskChainFactory().newChain().sync(() -> entities.forEach(ent -> {
                        if (ent.equals(player) || !EntityFilter.check(ent)) return;
                        var le = (LivingEntity) ent;
                        le.setVelocity(player.getLocation().toVector()
                                .subtract(ent.getLocation().toVector()).normalize());
                        le.damage(damage, player);
                    })).execute();
                }
            }
        });
    }

    @Override
    public void onInitConfig(){
        Map<String, Object> map = new HashMap<>();
        map.put("cooldown", "{level}*18+12");
        map.put("amount", "{level}");
        map.put("distance", "10+{level}*2");
        map.put("damage", "{level}*1.5");
        initConfigEntries(map);
    }
}
