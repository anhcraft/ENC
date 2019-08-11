package dev.anhcraft.enc.enchantments;

import dev.anhcraft.craftkit.utils.PlayerUtil;
import dev.anhcraft.enc.ENC;
import dev.anhcraft.enc.api.Enchantment;
import dev.anhcraft.enc.api.ItemReport;
import dev.anhcraft.enc.api.listeners.AsyncAttackListener;
import dev.anhcraft.enc.utils.PlayerMap;
import dev.anhcraft.enc.utils.UnitUtil;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class Freeze extends Enchantment {
    private final PlayerMap<Long> DATA = new PlayerMap<>();

    public Freeze() {
        super("Freeze", new String[]{
                "There is a chance to freeze your enemy in a short time"
        }, "anhcraft", null, 3, EnchantmentTarget.ALL);
        
        new BukkitRunnable() {
            @Override
            public void run() {
                Iterator<Map.Entry<Player, Long>> entries = DATA.entrySet().iterator();
                while(entries.hasNext()){
                    Map.Entry<Player, Long> entry = entries.next();
                    if(System.currentTimeMillis() > entry.getValue()){
                        PlayerUtil.unfreeze(entry.getKey());
                        entries.remove();
                    }
                }
            }
        }.runTaskTimerAsynchronously(ENC.getInstance(), 0, 20);

        getEventListeners().add(new AsyncAttackListener() {
            @Override
            public void onAttack(ItemReport mainHand, EntityDamageByEntityEvent event, LivingEntity entity) {
                if(event.isCancelled() || !(entity instanceof Player)) return;
                Player pent = (Player) entity;
                if(DATA.containsKey(pent)) return;
                double chance = computeConfigValue("chance", mainHand)/100d;
                if(Math.random() > chance) return;
                PlayerUtil.freeze(pent);
                long duration = (long) UnitUtil.tick2ms(computeConfigValue("duration", mainHand));
                DATA.put(pent, System.currentTimeMillis()+duration);
            }
        });
    }

    @Override
    public void onInitConfig(){
        Map<String, Object> map = new HashMap<>();
        map.put("chance", "{level}*12");
        map.put("duration", "{level}*50");
        initConfigEntries(map);
    }
}
