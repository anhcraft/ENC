package org.anhcraft.enc.listeners;

import org.anhcraft.enc.ENC;
import org.anhcraft.enc.api.rune.Rune;
import org.anhcraft.enc.api.rune.RuneAPI;
import org.anhcraft.enc.api.rune.RuneItem;
import org.anhcraft.enc.utils.ReplaceUtils;
import org.anhcraft.enc.utils.RouletteSelect;
import org.anhcraft.spaciouslib.utils.CommonUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class RuneDropListener implements Listener {
    private static final List<String> KILL_MOBS_WORLDS = new ArrayList<>();
    private static final List<EntityType> KILL_MOBS_ENTITIES = new ArrayList<>();

    public static void init(){
        KILL_MOBS_WORLDS.clear();
        HashMap<String, List<String>> group1 = new HashMap<>();
        group1.put("all", Bukkit.getWorlds().stream().map(World::getName).collect(Collectors.toList()));
        KILL_MOBS_WORLDS.addAll(ReplaceUtils.replaceVariables(
                ENC.getGeneralConfig().getStringList("rune.rune_drop.kill_mobs.worlds"),
                group1, true));

        KILL_MOBS_ENTITIES.clear();
        HashMap<String, List<String>> group2 = new HashMap<>();
        group2.put("entities", getEntityTypes(LivingEntity.class));
        group2.put("animals", getEntityTypes(Animals.class));
        group2.put("monsters", getEntityTypes(Monster.class));
        KILL_MOBS_ENTITIES.addAll(ReplaceUtils.replaceVariables(
                ENC.getGeneralConfig().getStringList("rune.rune_drop.kill_mobs.entity_types"),
                group2, false)
                .stream()
                .map(EntityType::valueOf)
                .collect(Collectors.toList()));
    }

    private static List<String> getEntityTypes(Class<?> c) {
        return CommonUtils.toList(EntityType.values())
                .stream()
                .filter(entityType -> entityType.getEntityClass() != null
                        && c.isAssignableFrom(entityType.getEntityClass()))
                .map(entityType -> entityType.name().toLowerCase())
                .collect(Collectors.toList());
    }

    @EventHandler(ignoreCancelled = true)
    public void kill(EntityDeathEvent event){
        if(ENC.getGeneralConfig().getBoolean("rune.rune_drop.kill_mobs.enabled") &&
                event.getEntity().getKiller() != null &&
                KILL_MOBS_WORLDS.contains(event.getEntity().getWorld().getName()) &&
                KILL_MOBS_ENTITIES.contains(event.getEntity().getType())) {
            Player killer = event.getEntity().getKiller();
            if(ENC.getGeneralConfig().getBoolean("rune.rune_drop.kill_mobs.need_permission")
                    && !killer.hasPermission("enc.rune_drop.kill_mobs")){
                return;
            }
            List<Rune> runes = RuneAPI.getRegisteredRunes();
            List<Double> rates = runes.stream().map(Rune::getDropRate).collect(Collectors.toList());
            rates.add(ENC.getGeneralConfig().getDouble("rune.rune_drop.kill_mobs.no_drop_rate"));
            int in = RouletteSelect.chooseFromList(rates);
            if(in == runes.size()){
                return;
            }
            Rune rune = runes.get(in);
            ItemStack itemStack = new ItemStack(Material.EMERALD, 1);
            RuneAPI.assignRune(itemStack, new RuneItem(rune));
            event.getDrops().add(itemStack);
        }
    }
}
