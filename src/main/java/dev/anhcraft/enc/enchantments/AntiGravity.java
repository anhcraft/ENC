package dev.anhcraft.enc.enchantments;

import dev.anhcraft.craftkit.events.ArmorChangeEvent;
import dev.anhcraft.craftkit.events.ArmorEquipEvent;
import dev.anhcraft.craftkit.events.ArmorUnequipEvent;
import dev.anhcraft.enc.api.Enchantment;
import dev.anhcraft.enc.api.ItemReport;
import dev.anhcraft.enc.api.listeners.SyncChangeEquipListener;
import dev.anhcraft.enc.api.listeners.SyncEquipListener;
import dev.anhcraft.enc.api.listeners.SyncUnequipListener;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashMap;
import java.util.Map;

public class AntiGravity extends Enchantment {
    public AntiGravity() {
        super("AntiGravity", new String[]{
                " You can jump higher"
        }, "anhcraft", null, 4, EnchantmentTarget.ARMOR);

        getEventListeners().add(new SyncEquipListener() {
            @Override
            public void onEquip(ItemReport equip, ArmorEquipEvent event) {
                equip.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 999999, (int) computeConfigValue("effect_level", equip)));
            }
        });

        getEventListeners().add(new SyncUnequipListener() {
            @Override
            public void onUnequip(ItemReport equip, ArmorUnequipEvent event) {
                equip.getPlayer().removePotionEffect(PotionEffectType.JUMP);
            }
        });

        getEventListeners().add(new SyncChangeEquipListener() {
            @Override
            public void onChangeEquip(ItemReport oldEquip, ItemReport newEquip, ArmorChangeEvent event, boolean onOldEquip) {
                if(onOldEquip) event.getPlayer().removePotionEffect(PotionEffectType.JUMP);
                else event.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 999999, (int) computeConfigValue("effect_level", newEquip)));
            }
        });
    }

    @Override
    public void onInitConfig(){
        Map<String, Object> map = new HashMap<>();
        map.put("effect_level", "{level}-1");
        initConfigEntries(map);
    }
}
