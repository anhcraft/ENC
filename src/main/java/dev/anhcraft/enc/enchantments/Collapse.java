package dev.anhcraft.enc.enchantments;

import dev.anhcraft.craftkit.events.PlayerJumpEvent;
import dev.anhcraft.craftkit.utils.BlockUtil;
import dev.anhcraft.craftkit.utils.LocationUtil;
import dev.anhcraft.enc.ENC;
import dev.anhcraft.enc.api.Enchantment;
import dev.anhcraft.enc.api.ItemReport;
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
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.util.noise.SimplexOctaveGenerator;

import java.util.*;
import java.util.stream.Collectors;

public class Collapse extends Enchantment implements Listener {
    private final List<Material> MATERIAL_LIST = new ArrayList<>();
    private final Map<Location, Long> DAMAGED_BLOCKS = new HashMap<>();
    private final List<Integer> FALLING_BLOCKS = new ArrayList<>();

    public Collapse() {
        super("Collapse", new String[]{
                "Jumping on the spot twice causes nearby blocks to be collapsed"
        }, "anhcraft", null, 3, EnchantmentTarget.ARMOR_FEET);

        Bukkit.getPluginManager().registerEvents(this, ENC.getInstance());

        getEventListeners().add(new AsyncJumpListener() {
            @Override
            public void onJump(ItemReport foot, PlayerJumpEvent event) {
                if(!event.isOnSpot()) return;
                List<Block> breakBlocks = new ArrayList<>();
                boolean allowedMaterialList = getConfig().getBoolean("allowed_material_list");
                boolean falling = getConfig().getBoolean("block_falling");
                boolean physics = getConfig().getBoolean("block_physics");
                boolean sound = getConfig().getBoolean("sound");
                int r = RandomUtil.randomInt(
                        (int) computeConfigValue("min_affected_radius", foot),
                        (int) computeConfigValue("max_affected_radius", foot));
                double min_h = computeConfigValue("min_affected_depth", foot);
                double max_h = computeConfigValue("max_affected_depth", foot);
                double del_h = max_h-min_h;
                long timeout = (long) UnitUtil.tick2ms(computeConfigValue("timeout", foot));

                SimplexOctaveGenerator gen = new SimplexOctaveGenerator(foot.getPlayer().getWorld().getSeed(), 5);
                Location loc = foot.getPlayer().getLocation();
                List<Location> locs = LocationUtil.getNearbyLocations(loc, r, 0, r);
                for(Location xloc : locs){
                    // use noise to calculate the depth
                    double y = (gen.noise(xloc.getX(), xloc.getZ(),
                            0, 0, 0.01, 0.08, true)+1)/2d * del_h + min_h;
                    for(int f = 0; f < y; f++) {
                        // the block being damaged or broken
                        Location cloc = xloc.clone().subtract(0, f, 0);
                        if(allowedMaterialList != MATERIAL_LIST.contains(cloc.getBlock().getType())) continue;
                        long currentTime = System.currentTimeMillis();
                        if(DAMAGED_BLOCKS.containsKey(cloc)){
                            Long lastTime = DAMAGED_BLOCKS.get(cloc);
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
                List<Player> viewers = foot.getPlayer().getWorld().getNearbyEntities(loc, 30, 30, 30)
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
                                    FallingBlock fb = block1.getWorld().spawnFallingBlock(block1.getLocation(),
                                            block1.getType(), block1.getData());
                                    fb.setDropItem(false);
                                    FALLING_BLOCKS.add(fb.getEntityId());
                                }
                                block1.setType(Material.AIR, physics);
                            });
                            if(sound && !breakBlocks.isEmpty()) foot.getPlayer().playSound(foot.getPlayer().getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 3f, 1f);
                        })
                        .execute();
            }
        });
    }

    @Override
    public void onInitConfig(){
        Map<String, Object> map = new HashMap<>();
        map.put("min_affected_radius", "2");
        map.put("max_affected_radius", "{level}*2+2");
        map.put("min_affected_depth", "{level}");
        map.put("max_affected_depth", "{level}*2");
        map.put("timeout", "30");
        map.put("block_physics", true);
        map.put("block_falling", true);
        map.put("sound", true);
        map.put("material_list", Arrays.asList(
                "$solid",
                "-bedrock",
                "-barrier"));
        map.put("allowed_material_list", true);
        initConfigEntries(map);

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
        if(FALLING_BLOCKS.contains(event.getEntity().getEntityId())) event.setCancelled(true);
    }
}
