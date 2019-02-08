package org.anhcraft.enc.listeners;

import org.anhcraft.enc.ENC;
import org.anhcraft.enc.api.rune.Rune;
import org.anhcraft.enc.api.rune.RuneAPI;
import org.anhcraft.enc.api.rune.RuneItem;
import org.anhcraft.spaciouslib.utils.Cooldown;
import org.anhcraft.spaciouslib.utils.Group;
import org.anhcraft.spaciouslib.utils.InventoryUtils;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.UUID;

public class RuneApplyListener implements Listener {
    private static final HashMap<UUID, Group<Integer, Cooldown>> SWAP_COOLDOWN = new HashMap<>();

    @EventHandler
    public void swap(PlayerSwapHandItemsEvent event){
        if(!ENC.getGeneralConfig().getBoolean("rune.rune_apply.swap_items.enabled")){
            return;
        }
        if(ENC.getGeneralConfig().getBoolean("rune.rune_apply.swap_items.need_permission")
                && !event.getPlayer().hasPermission("enc.rune_apply.swap_items")){
            return;
        }
        Player p = event.getPlayer();
        if(InventoryUtils.isNull(p.getInventory().getItemInMainHand()) ||
                InventoryUtils.isNull(p.getInventory().getItemInOffHand())){
            return;
        }
        UUID u = p.getUniqueId();
        int times = ENC.getGeneralConfig().getInt("rune.rune_apply.swap_items.times");
        double delay = ENC.getGeneralConfig().getInt("rune.rune_apply.swap_items.delay")/20d;
        // if required times is higher than 1
        if(times > 1){
            // if the player is already in swapping
            if(SWAP_COOLDOWN.containsKey(u)){
                Group<Integer, Cooldown> x = SWAP_COOLDOWN.get(u);
                // if timeout, rejects previous tries
                if(x.getB().isTimeout(delay)){
                    SWAP_COOLDOWN.remove(u);
                    return;
                }
                // increase the times by one
                x.setA(x.getA()+1);
                // if reached the number of required times
                if(x.getA() == times){
                    SWAP_COOLDOWN.remove(u);
                } else { // if not, reset the cooldown timer
                    x.getB().reset();
                    return;
                }
            } else { // if this is the first swapping time
                SWAP_COOLDOWN.put(u, new Group<>(1, new Cooldown()));
                return;
            }
        }

        Group<ItemStack, ItemStack> result = applySwap(p, event.getMainHandItem(), event.getOffHandItem());
        if(result == null){
            return;
        }
        event.setMainHandItem(result.getA());
        event.setOffHandItem(result.getB());
    }

    private static Group<ItemStack, ItemStack> applySwap(Player p, ItemStack main, ItemStack off) {
        RuneItem rune1 = RuneAPI.searchRune(main);
        RuneItem rune2 = RuneAPI.searchRune(off);
        if(rune1 == null){
            if(rune2 == null){
                return null;
            }
            /*
            rune1: ITEM
            rune2: RUNE
             */
            if(ENC.getGeneralConfig().getBoolean("rune.rune_apply.swap_items.strict_override") && rune2.getRune().getEnchantmentLevel() <= ENC.getApi().getEnchantmentLevel(main, rune2.getRune().getEnchantment())){
                ENC.getPluginChat().sendPlayer(ENC.getLocaleConfig().getString("cancelled_apply_rune_attempt"), p);
                return null;
            }
            Rune.ApplyResult result = RuneAPI.tryApplyRune(rune2);
            ENC.getPluginChat().sendPlayer(ENC.getLocaleConfig().getString("applied_rune_"+result.name().toLowerCase()), p);
            switch(result){
                case SUCCESS:
                    off.setAmount(off.getAmount()-1);
                    ENC.getApi().addEnchantment(main,
                            rune2.getRune().getEnchantment(), rune2.getRune().getEnchantmentLevel());
                    if(ENC.getGeneralConfig().getBoolean("rune.rune_apply.swap_items.sound")){
                        p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 3f, 1f);
                    }
                    break;
                case FAILURE:
                    off.setAmount(off.getAmount()-1);
                    if(ENC.getGeneralConfig().getBoolean("rune.rune_apply.swap_items.sound")){
                        p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 3f, 1f);
                    }
                    break;
                case BROKEN:
                    off.setAmount(off.getAmount()-1);
                    main = null;
                    if(ENC.getGeneralConfig().getBoolean("rune.rune_apply.swap_items.sound")){
                        p.playSound(p.getLocation(), Sound.BLOCK_ANVIL_DESTROY, 3f, 1f);
                    }
                    break;
            }
        } else {
            if(rune2 != null){
                return null;
            }
            /*
            rune1: RUNE
            rune2: ITEM
             */
            if(ENC.getGeneralConfig().getBoolean("rune.rune_apply.swap_items.strict_override") && rune1.getRune().getEnchantmentLevel() <= ENC.getApi().getEnchantmentLevel(off, rune1.getRune().getEnchantment())){
                ENC.getPluginChat().sendPlayer(ENC.getLocaleConfig().getString("cancelled_apply_rune_attempt"), p);
                return null;
            }
            Rune.ApplyResult result = RuneAPI.tryApplyRune(rune1);
            ENC.getPluginChat().sendPlayer(ENC.getLocaleConfig().getString("applied_rune_"+result.name().toLowerCase()), p);
            switch(result){
                case SUCCESS:
                    main.setAmount(main.getAmount()-1);
                    ENC.getApi().addEnchantment(off,
                            rune1.getRune().getEnchantment(), rune1.getRune().getEnchantmentLevel());
                    if(ENC.getGeneralConfig().getBoolean("rune.rune_apply.swap_items.sound")){
                        p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 3f, 1f);
                    }
                    break;
                case FAILURE:
                    main.setAmount(main.getAmount()-1);
                    if(ENC.getGeneralConfig().getBoolean("rune.rune_apply.swap_items.sound")){
                        p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 3f, 1f);
                    }
                    break;
                case BROKEN:
                    main.setAmount(main.getAmount()-1);
                    off = null;
                    if(ENC.getGeneralConfig().getBoolean("rune.rune_apply.swap_items.sound")){
                        p.playSound(p.getLocation(), Sound.BLOCK_ANVIL_DESTROY, 3f, 1f);
                    }
                    break;
            }
        }
        return new Group<>(main, off);
    }
}
