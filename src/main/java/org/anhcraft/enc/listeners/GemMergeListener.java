package org.anhcraft.enc.listeners;

import org.anhcraft.enc.ENC;
import org.anhcraft.enc.api.EnchantmentAPI;
import org.anhcraft.enc.api.gem.GemAPI;
import org.anhcraft.enc.api.gem.GemItem;
import org.anhcraft.enc.api.gem.MergeResult;
import org.anhcraft.spaciouslib.annotations.AnnotationHandler;
import org.anhcraft.spaciouslib.annotations.PlayerCleaner;
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

public class GemMergeListener implements Listener {
    @PlayerCleaner
    private static final HashMap<UUID, Group<Integer, Cooldown>> SWAP_COOLDOWN = new HashMap<>();

    public GemMergeListener(){
        AnnotationHandler.register(GemMergeListener.class, null);
    }

    @EventHandler
    public void swap(PlayerSwapHandItemsEvent event){
        if(!ENC.getGeneralConfig().getBoolean("gem.gem_merge.swap_items.enabled")){
            return;
        }
        if(ENC.getGeneralConfig().getBoolean("gem.gem_merge.swap_items.need_permission")
                && !event.getPlayer().hasPermission("enc.gem_merge.swap_items")){
            return;
        }
        Player p = event.getPlayer();
        if(InventoryUtils.isNull(p.getInventory().getItemInMainHand()) ||
                InventoryUtils.isNull(p.getInventory().getItemInOffHand())){
            return;
        }
        UUID u = p.getUniqueId();
        int times = ENC.getGeneralConfig().getInt("gem.gem_merge.swap_items.times");
        double delay = ENC.getGeneralConfig().getInt("gem.gem_merge.swap_items.delay")/20d;
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

        Group<ItemStack, ItemStack> result = mergeSwap(p, event.getMainHandItem(), event.getOffHandItem());
        if(result == null){
            return;
        }
        event.setMainHandItem(result.getA());
        event.setOffHandItem(result.getB());
    }

    private static Group<ItemStack, ItemStack> mergeSwap(Player p, ItemStack main, ItemStack off) {
        GemItem gem1 = GemAPI.searchGem(main);
        GemItem gem2 = GemAPI.searchGem(off);
        if(gem1 == null){
            if(gem2 == null){
                return null;
            }
            /*
            gem1: ITEM
            gem2: GEM
             */
            if(ENC.getGeneralConfig().getBoolean("gem.gem_merge.swap_items.strict_override") && gem2.getGem().getEnchantmentLevel() <= EnchantmentAPI.getEnchantmentLevel(main, gem2.getGem().getEnchantment())){
                ENC.getPluginChat().sendPlayer(ENC.getLocaleConfig().getString("cancelled_merge_gem_attempt"), p);
                return null;
            }
            MergeResult result = merge(gem2);
            ENC.getPluginChat().sendPlayer(ENC.getLocaleConfig().getString("applied_gem_"+result.name().toLowerCase()), p);
            off.setAmount(off.getAmount()-1);
            switch(result){
                case SUCCESS:
                    EnchantmentAPI.addEnchantment(main,
                            gem2.getGem().getEnchantment(), gem2.getGem().getEnchantmentLevel());
                    if(ENC.getGeneralConfig().getBoolean("gem.gem_merge.swap_items.sound")){
                        p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 3f, 1f);
                    }
                    break;
                case FAILURE:
                    if(ENC.getGeneralConfig().getBoolean("gem.gem_merge.swap_items.sound")){
                        p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 3f, 1f);
                    }
                    break;
                case DESTRUCTION:
                    main = null;
                    if(ENC.getGeneralConfig().getBoolean("gem.gem_merge.swap_items.sound")){
                        p.playSound(p.getLocation(), Sound.BLOCK_ANVIL_DESTROY, 3f, 1f);
                    }
                    break;
            }
        } else {
            if(gem2 != null){
                return null;
            }
            /*
            gem1: GEM
            gem2: ITEM
             */
            if(ENC.getGeneralConfig().getBoolean("gem.gem_merge.swap_items.strict_override") && gem1.getGem().getEnchantmentLevel() <= EnchantmentAPI.getEnchantmentLevel(off, gem1.getGem().getEnchantment())){
                ENC.getPluginChat().sendPlayer(ENC.getLocaleConfig().getString("cancelled_merge_gem_attempt"), p);
                return null;
            }
            MergeResult result = merge(gem1);
            ENC.getPluginChat().sendPlayer(ENC.getLocaleConfig().getString("applied_gem_"+result.name().toLowerCase()), p);
            main.setAmount(main.getAmount()-1);
            switch(result){
                case SUCCESS:
                    EnchantmentAPI.addEnchantment(off,
                            gem1.getGem().getEnchantment(), gem1.getGem().getEnchantmentLevel());
                    if(ENC.getGeneralConfig().getBoolean("gem.gem_merge.swap_items.sound")){
                        p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 3f, 1f);
                    }
                    break;
                case FAILURE:
                    if(ENC.getGeneralConfig().getBoolean("gem.gem_merge.swap_items.sound")){
                        p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 3f, 1f);
                    }
                    break;
                case DESTRUCTION:
                    off = null;
                    if(ENC.getGeneralConfig().getBoolean("gem.gem_merge.swap_items.sound")){
                        p.playSound(p.getLocation(), Sound.BLOCK_ANVIL_DESTROY, 3f, 1f);
                    }
                    break;
            }
        }
        return new Group<>(main, off);
    }

    private static MergeResult merge(GemItem gem) {
        if(Math.random() <= gem.getSuccessRate()/100d){
            return MergeResult.SUCCESS;
        } else {
            if(Math.random() <= gem.getProtectionRate()/100d){
                return MergeResult.FAILURE;
            } else {
                return MergeResult.DESTRUCTION;
            }
        }
    }
}
