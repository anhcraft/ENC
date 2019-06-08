package dev.anhcraft.enc.enchantments;

import dev.anhcraft.craftkit.cb_common.lang.enumeration.NMSVersion;
import dev.anhcraft.enc.api.ActionReport;
import dev.anhcraft.enc.api.Enchantment;
import dev.anhcraft.enc.api.listeners.SyncKillListener;
import org.bukkit.Material;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class Chef extends Enchantment {
    private static final Material PORK = NMSVersion.getNMSVersion().isNewerOrSame(NMSVersion.v1_13_R1) ? Material.valueOf("COOKED_PORKCHOP") : Material.GRILLED_PORK;
    private static final Material COOKED_FISH = NMSVersion.getNMSVersion().isNewerOrSame(NMSVersion.v1_13_R1) ? null : Material.COOKED_FISH;

    public Chef() {
        super("Chef", new String[]{
                "Kills entities and cooks all dropped food"
        }, "anhcraft", null, 1, EnchantmentTarget.ALL);

        // we will make modification so that we must use the sync event
        getEventListeners().add(new SyncKillListener() {
            @Override
            public void onKill(ActionReport report, LivingEntity entity, List<ItemStack> drops) {
                for(var drop : drops) {
                    var mt = drop.getType().toString();
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
                            drop.setType(Material.valueOf("COOKED_COD"));
                            break;
                        case "SALMON":
                            drop.setType(Material.valueOf("COOKED_SALMON"));
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