package dev.anhcraft.enc.enchantments;

import dev.anhcraft.craftkit.utils.BlockUtil;
import dev.anhcraft.craftkit.utils.LocationUtil;
import dev.anhcraft.enc.ENC;
import dev.anhcraft.enc.api.ActionReport;
import dev.anhcraft.enc.api.Enchantment;
import dev.anhcraft.enc.api.listeners.AsyncJumpListener;
import dev.anhcraft.enc.utils.ReplaceUtil;
import dev.anhcraft.enc.utils.UnitUtil;
import dev.anhcraft.jvmkit.utils.ArrayUtil;
import dev.anhcraft.jvmkit.utils.RandomUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.util.noise.SimplexOctaveGenerator;

import java.util.*;
import java.util.stream.Collectors;

public class Collapse extends Enchantment implements Listener {
    private static final List<Material> MATERIAL_LIST = new ArrayList<>();
    private static final Map<Location, Long> DAMAGED_BLOCKS = new HashMap<>();
    private static final List<Integer> FALLING_BLOCKS = new ArrayList<>();

    public Collapse() {
        super("Collapse", new String[]{
                "Jumps on spot twice may cause blocks around to be collapsed"
        }, "anhcraft", null, 3, EnchantmentTarget.ARMOR_FEET);

        Bukkit.getPluginManager().registerEvents(this, ENC.getInstance());

        getEventListeners().add(new AsyncJumpListener() {
            @Override
            public void onJump(ActionReport report, boolean onSpot) {
                if(!onSpot) return;
                List<Block> breakBlocks = new ArrayList<>();
                var allowedMaterialList = getConfig().getBoolean("allowed_material_list");
                var falling = getConfig().getBoolean("block_falling");
                var physics = getConfig().getBoolean("block_physics");
                var sound = getConfig().getBoolean("sound");
                var r = RandomUtil.randomInt(
                        (int) computeConfigValue("min_affected_radius", report),
                        (int) computeConfigValue("max_affected_radius", report));
                var min_h = computeConfigValue("min_affected_depth", report);
                var max_h = computeConfigValue("max_affected_depth", report);
                var del_h = max_h-min_h;
                var timeout = (long) UnitUtil.tick2ms(computeConfigValue("timeout", report));

                var gen = new SimplexOctaveGenerator(report.getPlayer().getWorld().getSeed(), 5);
                var loc = report.getPlayer().getLocation();
                var locs = LocationUtil.getNearbyLocations(loc, r, 0, r);
                for(Location xloc : locs){
                    // use noise to calculate the depth
                    var y = (gen.noise(xloc.getX(), xloc.getZ(),
                            0, 0, 0.01, 0.08, true)+1)/2d * del_h + min_h;
                    for(var f = 0; f < y; f++) {
                        // the block being damaged or broken
                        var cloc = xloc.clone().subtract(0, f, 0);
                        if(allowedMaterialList != MATERIAL_LIST.contains(cloc.getBlock().getType())) continue;
                        var currentTime = System.currentTimeMillis();
                        if(DAMAGED_BLOCKS.containsKey(cloc)){
                            var lastTime = DAMAGED_BLOCKS.get(cloc);
                            // if not time out yet, this block is going to be broken
                            if(currentTime-lastTime <= timeout) breakBlocks.add(cloc.getBlock());
                            // clean old data
                            DAMAGED_BLOCKS.remove(cloc);
                        } else {
                            DAMAGED_BLOCKS.put(cloc, currentTime);
                        }
                    }
                }

                // reverse the order to put lowest blocks at first
                Collections.reverse(breakBlocks);
                List<Player> viewers = report.getPlayer().getWorld().getNearbyEntities(loc, 30, 30, 30)
                        .stream().filter(entity -> entity instanceof Player)
                        .map(entity -> (Player) entity).collect(Collectors.toList());
                ENC.getTaskChainFactory().newChain()
                        .async(() -> breakBlocks.forEach(block1 -> BlockUtil.createBreakAnimation(block1.hashCode(), block1, 1, viewers))).delay(10)
                        .async(() -> breakBlocks.forEach(block1 -> BlockUtil.createBreakAnimation(block1.hashCode(), block1, 3, viewers))).delay(10)
                        .async(() -> breakBlocks.forEach(block1 -> BlockUtil.createBreakAnimation(block1.hashCode(), block1, 5, viewers))).delay(10)
                        .async(() -> breakBlocks.forEach(block1 -> BlockUtil.createBreakAnimation(block1.hashCode(), block1, 7, viewers))).delay(10)
                        .async(() -> breakBlocks.forEach(block1 -> BlockUtil.createBreakAnimation(block1.hashCode(), block1, 9, viewers))).delay(10)
                        .sync(() -> {
                            breakBlocks.forEach(block1 -> {
                                if(falling) {
                                    var fb = block1.getWorld().spawnFallingBlock(block1.getLocation(),
                                            block1.getType(), block1.getData());
                                    fb.setDropItem(false);
                                    FALLING_BLOCKS.add(fb.getEntityId());
                                }
                                block1.setType(Material.AIR, physics);
                            });
                            if(sound && !breakBlocks.isEmpty()) report.getPlayer().playSound(report.getPlayer().getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 3f, 1f);
                        })
                        .execute();
            }
        });
    }

    @Override
    public void onRegistered(){
        Map<String, Object> map = new HashMap<>();
        map.put("min_affected_radius", "2");
        map.put("max_affected_radius", "{level}*2+2");
        map.put("min_affected_depth", "{level}");
        map.put("max_affected_depth", "{level}*2");
        map.put("timeout", "30");
        map.put("block_physics", true);
        map.put("block_falling", true);
        map.put("sound", true);
        map.put("material_list", new String[]{
                "$solid",
                "-bedrock",
                "-barrier"});
        map.put("allowed_material_list", true);
        initConfigEntries(map);
    }

    @Override
    public void onConfigReloaded(){
        MATERIAL_LIST.clear();
        HashMap<String, List<String>> groups = new HashMap<>();
        groups.put("all", ArrayUtil.toList(Material.values()).stream()
                .map(Material::name)
                .collect(Collectors.toList()));
        groups.put("solid", ArrayUtil.toList(Material.values()).stream()
                .filter(Material::isSolid)
                .map(Material::name)
                .collect(Collectors.toList()));
        MATERIAL_LIST.addAll(ReplaceUtil.replaceVariables(getConfig().getStringList("material_list"),
                groups, false).stream().map(Material::valueOf).collect(Collectors.toList()));
    }

    @EventHandler
    public void fall(EntityChangeBlockEvent event){
        if(FALLING_BLOCKS.contains(event.getEntity().getEntityId())){
            event.setCancelled(true);
        }
    }
}
