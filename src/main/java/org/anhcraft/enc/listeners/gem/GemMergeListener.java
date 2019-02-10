package org.anhcraft.enc.listeners.gem;

import org.anhcraft.enc.ENC;
import org.anhcraft.enc.api.EnchantmentAPI;
import org.anhcraft.enc.api.gem.GemAPI;
import org.anhcraft.enc.api.gem.GemItem;
import org.anhcraft.enc.api.gem.MergeResult;
import org.anhcraft.enc.utils.UnitUtils;
import org.anhcraft.spaciouslib.annotations.AnnotationHandler;
import org.anhcraft.spaciouslib.annotations.PlayerCleaner;
import org.anhcraft.spaciouslib.utils.Cooldown;
import org.anhcraft.spaciouslib.utils.Group;
import org.anhcraft.spaciouslib.utils.InventoryUtils;
import org.anhcraft.spaciouslib.utils.RandomUtils;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.UUID;

public class GemMergeListener implements Listener {
    public GemMergeListener(){
        AnnotationHandler.register(GemMergeListener.class, null);
    }

    private static MergeResult randomMergeResult(GemItem gem) {
        // we should use RandomUtils#randomDouble() instead of Math#random()
        if(RandomUtils.randomDouble(0, 1) <= gem.getSuccessRate()/100d){
            return MergeResult.SUCCESS;
        } else {
            return RandomUtils.randomDouble(0, 1) <= gem.getProtectionRate()/100d ?
                    MergeResult.FAILURE : MergeResult.DESTRUCTION;
        }
    }

    /*==================================
        GEM MERGE - SWAPPING ITEMS
    ==================================*/

    @PlayerCleaner
    private static final HashMap<UUID, Group<Integer, Cooldown>> SWAP_COUNTER = new HashMap<>();

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
        double delay = UnitUtils.tick2s(ENC.getGeneralConfig().getInt("gem.gem_merge.swap_items.delay"));
        if(times > 1){
            if(SWAP_COUNTER.containsKey(u)){
                Group<Integer, Cooldown> x = SWAP_COUNTER.get(u);
                if(x.getB().isTimeout(delay)){
                    // if time out, reject the player
                    SWAP_COUNTER.remove(u);
                    return; // prevent the merging below
                } else {
                    // increase the counter by 1
                    x.setA(x.getA() + 1);
                    if(x.getA() < times) {
                        x.getB().reset();
                        return; // prevent the merging below
                    } else {
                        SWAP_COUNTER.remove(u);
                    }
                }
            } else {
                // create a counter for the player
                SWAP_COUNTER.put(u, new Group<>(1, new Cooldown()));
                return; // prevent the merging below
            }
        }

        ItemStack a = event.getMainHandItem();
        ItemStack b = event.getOffHandItem();
        GemItem ga = GemAPI.searchGem(a);
        GemItem gb = GemAPI.searchGem(b);
        // if both are items or both are gems
        if((ga != null && gb != null) || (ga == null && gb == null)){
            return;
        }
        // A is an item => B is a gem
        if(ga == null){
            if(ENC.getGeneralConfig().getBoolean("gem.gem_merge.swap_items.strict_override")){
                int gemLv = gb.getGem().getEnchantmentLevel();
                int itemLevel = EnchantmentAPI.getEnchantmentLevel(a, gb.getGem().getEnchantment());
                if(gemLv <= itemLevel){
                    ENC.getPluginChat().sendPlayer(ENC.getLocaleConfig().getString("cancelled_merge_gem_attempt"), p);
                    return;
                }
            }

            b.setAmount(b.getAmount()-1);
            MergeResult result = randomMergeResult(gb);
            ENC.getPluginChat().sendPlayer(ENC.getLocaleConfig().getString("merged_gem_"+
                    result.name().toLowerCase()), p);
            switch(result){
                case SUCCESS:
                    EnchantmentAPI.addEnchantment(a, gb.getGem().getEnchantment(), gb.getGem().getEnchantmentLevel());
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
                    a = null;
                    if(ENC.getGeneralConfig().getBoolean("gem.gem_merge.swap_items.sound")){
                        p.playSound(p.getLocation(), Sound.BLOCK_ANVIL_DESTROY, 3f, 1f);
                    }
                    break;
            }
        } else {// B is an item => A is a gem
            if(ENC.getGeneralConfig().getBoolean("gem.gem_merge.swap_items.strict_override")){
                int gemLv = ga.getGem().getEnchantmentLevel();
                int itemLevel = EnchantmentAPI.getEnchantmentLevel(b, ga.getGem().getEnchantment());
                if(gemLv <= itemLevel){
                    ENC.getPluginChat().sendPlayer(ENC.getLocaleConfig().getString("cancelled_merge_gem_attempt"), p);
                    return;
                }
            }

            a.setAmount(a.getAmount()-1);
            MergeResult result = randomMergeResult(ga);
            ENC.getPluginChat().sendPlayer(ENC.getLocaleConfig().getString("merged_gem_"+
                    result.name().toLowerCase()), p);
            switch(result){
                case SUCCESS:
                    EnchantmentAPI.addEnchantment(b, ga.getGem().getEnchantment(), ga.getGem().getEnchantmentLevel());
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
                    b = null;
                    if(ENC.getGeneralConfig().getBoolean("gem.gem_merge.swap_items.sound")){
                        p.playSound(p.getLocation(), Sound.BLOCK_ANVIL_DESTROY, 3f, 1f);
                    }
                    break;
            }
        }

        event.setMainHandItem(a);
        event.setOffHandItem(b);
    }
}
