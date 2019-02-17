package org.anhcraft.enc.enchantments;

import org.anhcraft.enc.api.ActionReport;
import org.anhcraft.enc.api.Enchantment;
import org.anhcraft.enc.api.listeners.SyncKillListener;
import org.anhcraft.spaciouslib.utils.GameVersion;
import org.bukkit.Material;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class Chef extends Enchantment {
    private static final Material PORK = GameVersion.is1_13Above() ? Material.COOKED_PORKCHOP : Material.valueOf("GRILLED_PORK");
    private static final Material COOKED_FISH = GameVersion.is1_13Above() ? null : Material.valueOf("COOKED_FISH");

    public Chef() {
        super("Chef", new String[]{
                "Kills entities and cooks all dropped food"
        }, "anhcraft", null, 1, EnchantmentTarget.ALL);

        // we will make modification so that we must use the sync event
        getEventListeners().add(new SyncKillListener() {
            @Override
            public void onAttack(ActionReport report, LivingEntity entity, List<ItemStack> drops) {
                for(ItemStack drop : drops) {
                    String mt = drop.getType().toString();
                    switch(mt) {
                        case "PORKCHOP":
                        case "PORK":
                            drop.setType(PORK);
                            break;
                        case "RAW_BEEF":
                        case "BEEF":
                            drop.setType(Material.COOKED_BEEF);
                            break;
                        case "RAW_CHICKEN":
                        case "CHICKEN":
                            drop.setType(Material.COOKED_CHICKEN);
                            break;
                        case "COD":
                            drop.setType(Material.COOKED_COD);
                            break;
                        case "SALMON":
                            drop.setType(Material.COOKED_SALMON);
                            break;
                        case "RAW_FISH":
                            drop.setType(COOKED_FISH);
                            break;
                        case "MUTTON":
                            drop.setType(Material.COOKED_MUTTON);
                            break;
                        case "RABBIT":
                            drop.setType(Material.COOKED_RABBIT);
                            break;
                    }
                }
            }
        });
    }
}
