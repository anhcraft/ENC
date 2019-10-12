package dev.anhcraft.enc.enchant;

import dev.anhcraft.craftkit.events.ArmorChangeEvent;
import dev.anhcraft.craftkit.events.ArmorEquipEvent;
import dev.anhcraft.craftkit.events.ArmorUnequipEvent;
import dev.anhcraft.enc.ENC;
import dev.anhcraft.enc.api.Enchantment;
import dev.anhcraft.enc.api.ItemReport;
import dev.anhcraft.enc.api.handlers.ChangeEquipHandler;
import dev.anhcraft.enc.api.handlers.EquipHandler;
import dev.anhcraft.enc.api.handlers.UnequipHandler;
import dev.anhcraft.enc.utils.PlayerMap;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.stream.Collectors;

public class Antidote extends Enchantment {
    private final PlayerMap<Integer> MAP = new PlayerMap<>();
    private static final List<String> AVAILABLE_EFFECTS = Arrays.stream(PotionEffectType.values())
            .filter(Objects::nonNull)
            .map(PotionEffectType::getName)
            .collect(Collectors.toList());
    private final List<PotionEffectType> BAD_EFFECTS = new ArrayList<>();

    public Antidote() {
        super("Antidote", new String[]{
                "Clears away unlucky effects automatically"
        }, "anhcraft", null, 3, EnchantmentTarget.ARMOR_HEAD);

        new BukkitRunnable() {
            @Override
            public void run() {
                MAP.forEach((player, n) -> {
                    List<PotionEffectType> list = player.getActivePotionEffects().stream()
                            .map(PotionEffect::getType).collect(Collectors.toList());
                    list.retainAll(BAD_EFFECTS);
                    list.stream().limit(n).forEach(player::removePotionEffect);
                });
            }
        }.runTaskTimer(ENC.getInstance(), 0, 40);

        getEnchantHandlers().add(new EquipHandler() {
            @Override
            public void onEquip(ItemReport equip, ArmorEquipEvent event) {
                MAP.put(event.getPlayer(), (int) computeConfigValue("effect_pick_amount", equip));
            }
        });

        getEnchantHandlers().add(new UnequipHandler() {
            @Override
            public void onUnequip(ItemReport equip, ArmorUnequipEvent event) {
                MAP.remove(event.getPlayer());
            }
        });

        getEnchantHandlers().add(new ChangeEquipHandler() {
            @Override
            public void onChangeEquip(ItemReport oldEquip, ItemReport newEquip, ArmorChangeEvent event, boolean onOldEquip) {
                if(onOldEquip) MAP.remove(event.getPlayer());
                else MAP.put(event.getPlayer(), (int) computeConfigValue("effect_pick_amount", newEquip));
            }
        });
    }

    @Override
    public void onInitConfig(){
        Map<String, Object> map = new HashMap<>();
        map.put("effect_pick_amount", "{level}");
        map.put("effect_list", Arrays.asList(
                "BAD_OMEN",
                "BLINDNESS",
                "CONFUSION",
                "GLOWING",
                "HARM",
                "HUNGER",
                "LEVITATION",
                "POISON",
                "SLOW",
                "SLOW_DIGGING",
                "UNLUCK",
                "WEAKNESS",
                "WITHER"));
        initConfigEntries(map);

        BAD_EFFECTS.clear();
        List<String> effects = getConfig().getStringList("effect_list").stream()
                .map(String::toUpperCase)
                .collect(Collectors.toList());
        effects.retainAll(AVAILABLE_EFFECTS);
        BAD_EFFECTS.addAll(effects.stream()
                .map(PotionEffectType::getByName)
                .collect(Collectors.toList()));
    }
}
