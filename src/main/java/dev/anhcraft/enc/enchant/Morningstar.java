package dev.anhcraft.enc.enchant;

import co.aikar.taskchain.TaskChain;
import dev.anhcraft.craftkit.cb_common.NMSVersion;
import dev.anhcraft.craftkit.utils.VectorUtil;
import dev.anhcraft.enc.ENC;
import dev.anhcraft.enc.api.Enchantment;
import dev.anhcraft.enc.api.ItemReport;
import dev.anhcraft.enc.api.handlers.InteractHandler;
import dev.anhcraft.enc.utils.Cooldown;
import dev.anhcraft.enc.utils.EntityFilter;
import dev.anhcraft.enc.utils.PlayerMap;
import kotlin.Pair;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;

import java.util.*;

public class Morningstar extends Enchantment {
    private static final Material MATERIAL_PART = NMSVersion.current().compare(NMSVersion.v1_13_R1) >= 0 ? Material.valueOf("OAK_BUTTON") : Material.WOOD_BUTTON;
    private final PlayerMap<Cooldown> MAP = new PlayerMap<>();

    public Morningstar() {
        super("Morningstar", new String[]{
            "Creates a morningstar which rotates around you and hurt entities"
        }, "anhcraft", null, 5, EnchantmentTarget.TOOL);

        getEnchantHandlers().add(new InteractHandler() {
            @Override
            public void onInteract(ItemReport report, PlayerInteractEvent event) {
                Player player = report.getPlayer();
                if(Objects.equals(EquipmentSlot.OFF_HAND, event.getHand()) ||
                        (event.getAction() != Action.RIGHT_CLICK_BLOCK
                                && event.getAction() != Action.RIGHT_CLICK_AIR)) return;

                double cooldown = computeConfigValue("cooldown", report);
                if(!handleCooldown(MAP, player, cooldown)) return;

                Location p_loc = player.getLocation().add(0, 0.3, 0);
                double ms_bd_damage = computeConfigValue("morningstar.body_damage", report);
                double ms_hd_damage = computeConfigValue("morningstar.head_damage", report);
                double ms_rtt_spd = computeConfigValue("morningstar.rotate_speed", report);
                double ms_len = computeConfigValue("morningstar.length", report);
                double ms_pt_os = computeConfigValue("morningstar.part_offset", report);
                double ms_hd_os = computeConfigValue("morningstar.head_offset", report);
                double ms_dg_os = computeConfigValue("morningstar.degree_offset", report);
                Vector p_vec = player.getEyeLocation().getDirection().normalize();
                EulerAngle p_angle = new EulerAngle(p_vec.getX(), p_vec.getY(), p_vec.getZ());

                TaskChain<Object> task = ENC.getTaskChainFactory().newChain();
                List<Pair<ArmorStand, Vector>> parts = new LinkedList<>();

                task.sync(() -> {
                    // make the body
                    for(double i = 0d; i < ms_len; i+= ms_pt_os){
                        Vector pt_vec = p_vec.clone().multiply(i);
                        ArmorStand pt = player.getWorld().spawn(p_loc.clone().add(pt_vec), ArmorStand.class);
                        pt.setMarker(true);
                        pt.setGravity(false);
                        pt.setVisible(false);
                        pt.setSmall(true);
                        pt.setHeadPose(p_angle);
                        pt.setHelmet(new ItemStack(MATERIAL_PART, 1));
                        pt.setMetadata("enc",
                                new FixedMetadataValue(ENC.getInstance(), true));
                        parts.add(new Pair<>(pt, pt_vec));
                    }
                    // make the head
                    Vector h_vec = p_vec.clone().multiply(ms_len + ms_hd_os);
                    ArmorStand h = player.getWorld().spawn(p_loc.clone().add(h_vec), ArmorStand.class);
                    h.setMarker(true);
                    h.setGravity(false);
                    h.setVisible(false);
                    h.setSmall(true);
                    h.setHeadPose(p_angle);
                    h.setHelmet(new ItemStack(Material.IRON_BLOCK, 1));
                    h.setMetadata("enc", new FixedMetadataValue(ENC.getInstance(), true));
                    parts.add(new Pair<>(h, h_vec));
                });

                for(double r = 0d; r < 360; r += ms_dg_os){
                    final double r_ = r;
                    task.sync(() -> {
                        for(int i = 0; i < parts.size(); i++){
                            Pair<ArmorStand, Vector> ent = parts.get(i);
                            Location loc = p_loc.clone().add(VectorUtil.rotateAroundAxisY(
                                    ent.getSecond(), r_));
                            ent.getFirst().teleport(loc);
                            double dmg = (i == parts.size()-1) ? ms_hd_damage : ms_bd_damage;
                            double rs = ((i == parts.size()-1) ? ms_hd_os : ms_pt_os)*2;
                            ent.getFirst().getNearbyEntities(rs, rs, rs)
                                    .stream()
                                    .filter(EntityFilter::check)
                                    .filter(entity -> !entity.equals(player))
                                    .forEach(entity -> ((LivingEntity) entity).damage(dmg, player));
                        }
                    }).delay((int) (r/ms_rtt_spd));
                }
                task.sync(() -> parts.forEach(x -> x.getFirst().remove())).execute();
            }
        });
    }

    @Override
    public void onInitConfig(){
        Map<String, Object> map = new HashMap<>();
        map.put("cooldown", "{level}*18+35");
        map.put("morningstar.length", "5+{level}*0.5");
        map.put("morningstar.rotate_speed", "65");
        map.put("morningstar.part_offset", "0.12");
        map.put("morningstar.head_offset", "0.3");
        map.put("morningstar.degree_offset", "15");
        map.put("morningstar.body_damage", "{level}*1.5");
        map.put("morningstar.head_damage", "{level}*2");
        initConfigEntries(map);
    }
}
