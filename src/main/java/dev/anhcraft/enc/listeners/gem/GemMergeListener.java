package dev.anhcraft.enc.listeners.gem;

import dev.anhcraft.craftkit.utils.ItemUtil;
import dev.anhcraft.enc.ENC;
import dev.anhcraft.enc.api.EnchantmentAPI;
import dev.anhcraft.enc.api.gem.GemAPI;
import dev.anhcraft.enc.api.gem.GemItem;
import dev.anhcraft.enc.api.gem.MergeResult;
import dev.anhcraft.enc.utils.Cooldown;
import dev.anhcraft.enc.utils.PlayerMap;
import dev.anhcraft.enc.utils.UnitUtil;
import dev.anhcraft.jvmkit.utils.RandomUtil;
import kotlin.Pair;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.ItemStack;

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

    private final PlayerMap<Pair<AtomicInteger, Cooldown>> SWAP_COUNTER = new PlayerMap<>();

    @EventHandler
    public void swap(PlayerSwapHandItemsEvent event){
        if(!ENC.getGeneralConfig().getBoolean("gem.gem_merge.swap_items.enabled")) return;
        if(ENC.getGeneralConfig().getBoolean("gem.gem_merge.swap_items.need_permission")
                && !event.getPlayer().hasPermission("enc.gem_merge.swap_items")) return;
        Player p = event.getPlayer();
        if(ItemUtil.isNull(p.getInventory().getItemInMainHand()) || ItemUtil.isNull(p.getInventory().getItemInOffHand())) return;
        int times = ENC.getGeneralConfig().getInt("gem.gem_merge.swap_items.times");
        double delay = UnitUtil.tick2s(ENC.getGeneralConfig().getInt("gem.gem_merge.swap_items.delay"));
        if(times > 1){
            Pair<AtomicInteger, Cooldown> x = SWAP_COUNTER.get(p);
            if(x != null){
                if(x.getSecond().isTimeout(delay)){
                    // if time out, reject the player
                    SWAP_COUNTER.remove(p);
                    return; // prevent the merging below
                } else {
                    // increase the counter by 1
                    if(x.getFirst().incrementAndGet() < times) {
                        x.getSecond().reset();
                        return; // prevent the merging below
                    } else {
                        SWAP_COUNTER.remove(p);
                    }
                }
            } else {
                // create a counter for the player
                SWAP_COUNTER.put(p, new Pair<>(new AtomicInteger(1), new Cooldown()));
                return; // prevent the merging below
            }
        }

        ItemStack a = event.getMainHandItem();
        ItemStack b = event.getOffHandItem();
        GemItem ga = GemAPI.searchGem(a);
        GemItem gb = GemAPI.searchGem(b);
        // if both are items or both are gems
        if((ga != null && gb != null) || (ga == null && gb == null)) return;
        // A is int item => B is a gem
        if(ga == null){
            if(ENC.getGeneralConfig().getBoolean("gem.gem_merge.swap_items.strict_override")){
                int gemLv = gb.getGem().getEnchantmentLevel();
                int itemLevel = EnchantmentAPI.getEnchantmentLevel(a, gb.getGem().getEnchantment());
                if(gemLv <= itemLevel){
                    ENC.getPluginChat().message(p, ENC.getLocaleConfig().getString("cancelled_merge_gem_attempt"));
                    return;
                }
            }

            b.setAmount(b.getAmount()-1);
            MergeResult result = randomMergeResult(gb);
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
        } else {// B is int item => A is a gem
            if(ENC.getGeneralConfig().getBoolean("gem.gem_merge.swap_items.strict_override")){
                int gemLv = ga.getGem().getEnchantmentLevel();
                int itemLevel = EnchantmentAPI.getEnchantmentLevel(b, ga.getGem().getEnchantment());
                if(gemLv <= itemLevel){
                    ENC.getPluginChat().message(p, ENC.getLocaleConfig().getString("cancelled_merge_gem_attempt"));
                    return;
                }
            }

            a.setAmount(a.getAmount()-1);
            MergeResult result = randomMergeResult(ga);
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
