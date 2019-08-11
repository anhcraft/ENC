package dev.anhcraft.enc.enchantments;

import dev.anhcraft.enc.api.Enchantment;
import dev.anhcraft.enc.api.ItemReport;
import dev.anhcraft.enc.api.listeners.SyncDeathDropListener;
import org.bukkit.enchantments.EnchantmentTarget;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class Soulbound extends Enchantment {
    public Soulbound() {
        super("Soulbound", new String[]{
                "Has a chance to rescue your item from being dropped"
        }, "anhcraft", null, 5, EnchantmentTarget.ALL);

        // we will make modification so that we must use the sync event
        getEventListeners().add(new SyncDeathDropListener() {
            @Override
            public void onDrop(ItemReport report, AtomicBoolean keep) {
                if(!keep.get()) return;
                double chance = computeConfigValue("chance", report)/100d;
                keep.set(Math.random() <= chance);
            }
        });
    }

    @Override
    public void onInitConfig(){
        Map<String, Object> map = new HashMap<>();
        map.put("chance", "{level}*20");
        initConfigEntries(map);
    }
}
