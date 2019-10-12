package dev.anhcraft.enc.enchant;

import dev.anhcraft.craftkit.cb_common.NMSVersion;
import dev.anhcraft.enc.ENC;
import dev.anhcraft.enc.api.Enchantment;
import dev.anhcraft.enc.api.ItemReport;
import dev.anhcraft.enc.api.handlers.InteractHandler;
import dev.anhcraft.enc.utils.Cooldown;
import dev.anhcraft.enc.utils.EntityFilter;
import dev.anhcraft.enc.utils.PlayerMap;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

public class Pull extends Enchantment {
    private static final Sound SOUND = NMSVersion.current().compare(NMSVersion.v1_13_R1) >= 0 ? Sound.valueOf("BLOCK_SLIME_BLOCK_HIT") : Sound.BLOCK_SLIME_HIT;
    private final PlayerMap<Cooldown> MAP = new PlayerMap<>();

    public Pull() {
        super("Pull", new String[]{
            "Pulls entities toward you"
        }, "anhcraft", null, 3, EnchantmentTarget.ALL);

        getEnchantHandlers().add(new InteractHandler() {
            @Override
            public void onInteract(ItemReport report, PlayerInteractEvent event) {
                Player player = report.getPlayer();
                if(Objects.equals(EquipmentSlot.OFF_HAND, event.getHand()) ||
                        (event.getAction() != Action.RIGHT_CLICK_BLOCK
                                && event.getAction() != Action.RIGHT_CLICK_AIR)) return;

                double cooldown = computeConfigValue("cooldown", report);
                if(!handleCooldown(MAP, player, cooldown)) return;

                int amount = (int) computeConfigValue("amount", report);
                double distance = computeConfigValue("distance", report);
                double damage = computeConfigValue("damage", report);
                Location loc = player.getEyeLocation();

                player.playSound(player.getLocation(), SOUND, 3f, 1f);
                for (int g = 1; g < distance; g++) {
                    Location target = loc.clone().add(loc.getDirection().normalize().multiply(g));
                    ENC.getEffectManager().display(Particle.VILLAGER_HAPPY, target, 0, 0, 0, 0, 10, 3, null, null, (byte) 0, 50, target.getWorld().getPlayers());
                    Stream<Entity> entities = player.getWorld().getNearbyEntities(target, 1.5, 1.5, 1.5)
                            .stream().limit(amount);
                    ENC.getTaskChainFactory().newChain().sync(() -> entities.forEach(ent -> {
                        if (ent.equals(player) || !EntityFilter.check(ent)) return;
                        LivingEntity le = (LivingEntity) ent;
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
