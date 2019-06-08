package dev.anhcraft.enc.enchantments;

import dev.anhcraft.craftkit.common.lang.annotation.RequiredCleaner;
import dev.anhcraft.craftkit.utils.PlayerUtil;
import dev.anhcraft.enc.ENC;
import dev.anhcraft.enc.api.ActionReport;
import dev.anhcraft.enc.api.Enchantment;
import dev.anhcraft.enc.api.listeners.AsyncAttackListener;
import dev.anhcraft.enc.utils.UnitUtil;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;

public class Freeze extends Enchantment {
    @RequiredCleaner
    private static final Map<Player, Long> DATA = new HashMap<>();

    public Freeze() {
        super("Freeze", new String[]{
                "There is a chance to freeze your enemy in a short time"
        }, "anhcraft", null, 3, EnchantmentTarget.ALL);
        
        new BukkitRunnable() {
            @Override
            public void run() {
                var entries = DATA.entrySet().iterator();
                while(entries.hasNext()){
                    var entry = entries.next();
                    if(System.currentTimeMillis() > entry.getValue()){
                        PlayerUtil.unfreeze(entry.getKey());
                        entries.remove();
                    }
                }
            }
        }.runTaskTimerAsynchronously(ENC.getInstance(), 0, 20);

        getEventListeners().add(new AsyncAttackListener() {
            @Override
            public void onAttack(ActionReport report, LivingEntity entity, double damage) {
                if(report.isPrevented() || !(entity instanceof Player)) return;
                var pent = (Player) entity;
                if(DATA.containsKey(pent)) return;
                var chance = computeConfigValue("chance", report)/100d;
                if(Math.random() > chance) return;
                PlayerUtil.freeze(pent);
                var duration = (long) UnitUtil.tick2ms(computeConfigValue("duration", report));
                DATA.put(pent, System.currentTimeMillis()+duration);
            }
        });
    }

    @Override
    public void onRegistered(){
        Map<String, Object> map = new HashMap<>();
        map.put("chance", "{level}*12");
        map.put("duration", "{level}*50");
        initConfigEntries(map);
    }
}