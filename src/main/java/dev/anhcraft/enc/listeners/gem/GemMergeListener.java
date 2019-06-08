package dev.anhcraft.enc.listeners.gem;

import dev.anhcraft.craftkit.common.lang.annotation.RequiredCleaner;
import dev.anhcraft.craftkit.utils.ItemUtil;
import dev.anhcraft.enc.ENC;
import dev.anhcraft.enc.api.EnchantmentAPI;
import dev.anhcraft.enc.api.gem.GemAPI;
import dev.anhcraft.enc.api.gem.GemItem;
import dev.anhcraft.enc.api.gem.MergeResult;
import dev.anhcraft.enc.utils.Cooldown;
import dev.anhcraft.enc.utils.UnitUtil;
import dev.anhcraft.jvmkit.utils.RandomUtil;
import kotlin.Pair;
import org.bukkit.Sound;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public class GemMergeListener implements Listener {
    private static MergeResult randomMergeResult(GemItem gem) {
        if(RandomUtil.randomDouble(0, 1) <= gem.getSuccessRate()/100d) return MergeResult.SUCCESS;
        else return RandomUtil.randomDouble(0, 1) <= gem.getProtectionRate()/100d ?
                    MergeResult.FAILURE : MergeResult.DESTRUCTION;
    }

    /*==================================
        GEM MERGE - SWAPPING ITEMS
    ==================================*/

    @RequiredCleaner
    private static final Map<UUID, Pair<AtomicInteger, Cooldown>> SWAP_COUNTER = new HashMap<>();

    @EventHandler
    public void swap(PlayerSwapHandItemsEvent event){
        if(!ENC.getGeneralConfig().getBoolean("gem.gem_merge.swap_items.enabled")) return;
        if(ENC.getGeneralConfig().getBoolean("gem.gem_merge.swap_items.need_permission")
                && !event.getPlayer().hasPermission("enc.gem_merge.swap_items")) return;
        var p = event.getPlayer();
        if(ItemUtil.isNull(p.getInventory().getItemInMainHand()) || ItemUtil.isNull(p.getInventory().getItemInOffHand())) return;
        var u = p.getUniqueId();
        var times = ENC.getGeneralConfig().getInt("gem.gem_merge.swap_items.times");
        var delay = UnitUtil.tick2s(ENC.getGeneralConfig().getInt("gem.gem_merge.swap_items.delay"));
        if(times > 1){
            if(SWAP_COUNTER.containsKey(u)){
                var x = SWAP_COUNTER.get(u);
                if(x.getSecond().isTimeout(delay)){
                    // if time out, reject the player
                    SWAP_COUNTER.remove(u);
                    return; // prevent the merging below
                } else {
                    // increase the counter by 1
                    if(x.getFirst().incrementAndGet() < times) {
                        x.getSecond().reset();
                        return; // prevent the merging below
                    } else {
                        SWAP_COUNTER.remove(u);
                    }
                }
            } else {
                // create a counter for the player
                SWAP_COUNTER.put(u, new Pair<>(new AtomicInteger(1), new Cooldown()));
                return; // prevent the merging below
            }
        }

        var a = event.getMainHandItem();
        var b = event.getOffHandItem();
        var ga = GemAPI.searchGem(a);
        var gb = GemAPI.searchGem(b);
        // if both are items or both are gems
        if((ga != null && gb != null) || (ga == null && gb == null)) return;
        // A is var item => B is a gem
        if(ga == null){
            if(ENC.getGeneralConfig().getBoolean("gem.gem_merge.swap_items.strict_override")){
                var gemLv = gb.getGem().getEnchantmentLevel();
                var itemLevel = EnchantmentAPI.getEnchantmentLevel(a, gb.getGem().getEnchantment());
                if(gemLv <= itemLevel){
                    ENC.getPluginChat().message(p, ENC.getLocaleConfig().getString("cancelled_merge_gem_attempt"));
                    return;
                }
            }

            b.setAmount(b.getAmount()-1);
            var result = randomMergeResult(gb);
            ENC.getPluginChat().message(p, ENC.getLocaleConfig().getString("merged_gem_"+
                    result.name().toLowerCase()));
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
        } else {// B is var item => A is a gem
            if(ENC.getGeneralConfig().getBoolean("gem.gem_merge.swap_items.strict_override")){
                var gemLv = ga.getGem().getEnchantmentLevel();
                var itemLevel = EnchantmentAPI.getEnchantmentLevel(b, ga.getGem().getEnchantment());
                if(gemLv <= itemLevel){
                    ENC.getPluginChat().message(p, ENC.getLocaleConfig().getString("cancelled_merge_gem_attempt"));
                    return;
                }
            }

            a.setAmount(a.getAmount()-1);
            var result = randomMergeResult(ga);
            ENC.getPluginChat().message(p, ENC.getLocaleConfig().getString("merged_gem_"+
                    result.name().toLowerCase()));
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
