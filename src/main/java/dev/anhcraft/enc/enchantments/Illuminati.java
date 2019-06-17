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

public class Illuminati extends Enchantment {
    public Illuminati() {
        super("Illuminati", new String[]{
                "You can look through the darkness"
        }, "anhcraft", null, 1, EnchantmentTarget.ARMOR);

        getEventListeners().add(new SyncEquipListener() {
            @Override
            public void onEquip(ItemReport equip, ArmorEquipEvent event) {
                equip.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, 999999, 1));
            }
        });

        getEventListeners().add(new SyncUnequipListener() {
            @Override
            public void onUnequip(ItemReport equip, ArmorUnequipEvent event) {
                equip.getPlayer().removePotionEffect(PotionEffectType.NIGHT_VISION);
            }
        });

        getEventListeners().add(new SyncChangeEquipListener() {
            @Override
            public void onChangeEquip(ItemReport oldEquip, ItemReport newEquip, ArmorChangeEvent event, boolean onOldEquip) {
                if(onOldEquip) event.getPlayer().removePotionEffect(PotionEffectType.NIGHT_VISION);
                else event.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, 999999, 1));
            }
        });
    }
}
