package dev.anhcraft.enc.enchantments;

import dev.anhcraft.craftkit.cb_common.NMSVersion;
import dev.anhcraft.enc.api.Enchantment;
import dev.anhcraft.enc.api.ItemReport;
import dev.anhcraft.enc.api.listeners.SyncKillListener;
import org.bukkit.Material;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;

public class Chef extends Enchantment {
    private static final Material PORK = NMSVersion.current().compare(NMSVersion.v1_13_R1) >= 0 ? Material.valueOf("COOKED_PORKCHOP") : Material.GRILLED_PORK;
    private static final Material COOKED_FISH = NMSVersion.current().compare(NMSVersion.v1_13_R1) >= 0 ? null : Material.COOKED_FISH;

    public Chef() {
        super("Chef", new String[]{
                "Kills entities and cooks all dropped food"
        }, "anhcraft", null, 1, EnchantmentTarget.ALL);

        getEventListeners().add(new SyncKillListener() {
            @Override
            public void onKill(ItemReport mainHand, EntityDeathEvent event) {
                for(ItemStack drop : event.getDrops()) {
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
