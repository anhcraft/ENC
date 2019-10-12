package dev.anhcraft.enc.enchant;

import dev.anhcraft.craftkit.utils.BlockUtil;
import dev.anhcraft.enc.api.Enchantment;
import dev.anhcraft.enc.api.ItemReport;
import dev.anhcraft.enc.api.handlers.BreakBlockHandler;
import dev.anhcraft.enc.utils.Cooldown;
import dev.anhcraft.enc.utils.PlayerMap;
import dev.anhcraft.enc.utils.ReplaceUtil;
import dev.anhcraft.jvmkit.utils.ArrayUtil;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.event.block.BlockBreakEvent;

import java.util.*;
import java.util.stream.Collectors;

public class Digger extends Enchantment {
    private final PlayerMap<Cooldown> MAP = new PlayerMap<>();
    private final List<Material> MATERIAL_LIST = new ArrayList<>();

    public Digger() {
        super("Digger", new String[]{
                "Breaks multiple blocks at once"
        }, "anhcraft", null, 3, EnchantmentTarget.TOOL);

        getEnchantHandlers().add(new BreakBlockHandler() {
            @Override
            public void onBreakBlock(ItemReport mainHand, BlockBreakEvent event) {
                if(event.isCancelled()) return;
                event.setCancelled(true);
                double cooldown = computeConfigValue("cooldown", mainHand);
                if(!handleCooldown(MAP, event.getPlayer(), cooldown)) return;

                boolean sameType = getConfig().getBoolean("must_same_type");
                boolean allowedMaterialList = getConfig().getBoolean("allowed_material_list");
                int r = (int) computeConfigValue("radius", mainHand);
                List<Block> blocks = BlockUtil.getNearbyBlocks(event.getBlock().getLocation(), r, r, r);
                for(Block b : blocks){
                    if(sameType && !b.getType().equals(event.getBlock().getType())) continue;
                    if(allowedMaterialList == MATERIAL_LIST.contains(b.getType())) b.breakNaturally(mainHand.getItem());
                }
            }
        });
    }

    @Override
    public void onInitConfig(){
        Map<String, Object> map = new HashMap<>();
        map.put("radius", "{level}");
        map.put("must_same_type", false);
        map.put("material_list", Arrays.asList(
                "$solid",
                "-bedrock",
                "-barrier"));
        map.put("allowed_material_list", true);
        map.put("cooldown", "{level}*15+30");
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
}
