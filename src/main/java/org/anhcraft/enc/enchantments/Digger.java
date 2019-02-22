package org.anhcraft.enc.enchantments;

import org.anhcraft.enc.api.ActionReport;
import org.anhcraft.enc.api.Enchantment;
import org.anhcraft.enc.api.listeners.SyncBlockBreakListener;
import org.anhcraft.enc.utils.ReplaceUtils;
import org.anhcraft.spaciouslib.utils.BlockUtils;
import org.anhcraft.spaciouslib.utils.CommonUtils;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.enchantments.EnchantmentTarget;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class Digger extends Enchantment {
    private static final List<Material> MATERIAL_LIST = new ArrayList<>();

    public Digger() {
        super("Digger", new String[]{
                "Breaks multiple blocks at once"
        }, "anhcraft", null, 3, EnchantmentTarget.TOOL);

        getEventListeners().add(new SyncBlockBreakListener() {
            @Override
            public void onBreakBlock(ActionReport report, Block block) {
                if(report.isPrevented()){
                    return;
                }
                boolean sameType = getConfig().getBoolean("must_same_type");
                boolean allowedMaterialList = getConfig().getBoolean("allowed_material_list");
                int r = (int) computeConfigValue("radius", report);
                Block[] blocks = BlockUtils.getNearbyBlocks(block.getLocation(), r, r, r);
                for(Block b : blocks){
                    if(sameType && !b.getType().equals(block.getType())){
                        continue;
                    }
                    if(allowedMaterialList == MATERIAL_LIST.contains(b.getType())){
                        b.breakNaturally(report.getItemStack());
                    }
                }
                report.setPrevented(true);
            }
        });
    }

    @Override
    public void onRegistered(){
        HashMap<String, Object> map = new HashMap<>();
        map.put("radius", "{level}");
        map.put("must_same_type", false);
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
        groups.put("all", CommonUtils.toList(Material.values()).stream()
                .map(Material::name)
                .collect(Collectors.toList()));
        groups.put("solid", CommonUtils.toList(Material.values()).stream()
                .filter(Material::isSolid)
                .map(Material::name)
                .collect(Collectors.toList()));
        MATERIAL_LIST.addAll(ReplaceUtils.replaceVariables(getConfig().getStringList("material_list"),
                groups, false).stream().map(Material::valueOf).collect(Collectors.toList()));
    }
}
