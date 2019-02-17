package org.anhcraft.enc.enchantments;

import org.anhcraft.enc.api.ActionReport;
import org.anhcraft.enc.api.Enchantment;
import org.anhcraft.enc.api.listeners.SyncDeathDropListener;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;

public class Soulbound extends Enchantment {
    public Soulbound() {
        super("Soulbound", new String[]{
                "Has a chance to rescue your item from being dropped"
        }, "anhcraft", null, 5, EnchantmentTarget.ALL);

        // we will make modification so that we must use the sync event
        getEventListeners().add(new SyncDeathDropListener() {
            @Override
            public void onDrop(ActionReport report, ItemStack itemStack) {
                if(report.isPrevented()){
                    return;
                }
                double chance = computeConfigValue("chance", report)/100d;
                if(Math.random() <= chance){
                    report.setPrevented(true);
                }
            }
        });
    }

    @Override
    public void onRegistered(){
        HashMap<String, Object> map = new HashMap<>();
        map.put("chance", "{level}*20");
        initConfigEntries(map);
    }
}
