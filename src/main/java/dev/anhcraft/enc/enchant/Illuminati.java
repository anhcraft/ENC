package dev.anhcraft.enc.enchant;

import dev.anhcraft.craftkit.events.ArmorChangeEvent;
import dev.anhcraft.craftkit.events.ArmorEquipEvent;
import dev.anhcraft.craftkit.events.ArmorUnequipEvent;
import dev.anhcraft.enc.api.Enchantment;
import dev.anhcraft.enc.api.ItemReport;
import dev.anhcraft.enc.api.handlers.ChangeEquipHandler;
import dev.anhcraft.enc.api.handlers.EquipHandler;
import dev.anhcraft.enc.api.handlers.UnequipHandler;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class Illuminati extends Enchantment {
    public Illuminati() {
        super("Illuminati", new String[]{
                "You can look through the darkness"
        }, "anhcraft", null, 1, EnchantmentTarget.ARMOR);

        getEnchantHandlers().add(new EquipHandler() {
            @Override
            public void onEquip(ItemReport equip, ArmorEquipEvent event) {
                equip.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, 999999, 1));
            }
        });

        getEnchantHandlers().add(new UnequipHandler() {
            @Override
            public void onUnequip(ItemReport equip, ArmorUnequipEvent event) {
                equip.getPlayer().removePotionEffect(PotionEffectType.NIGHT_VISION);
            }
        });

        getEnchantHandlers().add(new ChangeEquipHandler() {
            @Override
            public void onChangeEquip(ItemReport oldEquip, ItemReport newEquip, ArmorChangeEvent event, boolean onOldEquip) {
                if(onOldEquip) event.getPlayer().removePotionEffect(PotionEffectType.NIGHT_VISION);
                else event.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, 999999, 1));
            }
        });
    }
}
