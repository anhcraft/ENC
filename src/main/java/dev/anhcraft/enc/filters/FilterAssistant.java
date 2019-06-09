package dev.anhcraft.enc.filters;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import dev.anhcraft.enc.filters.entity.NPCFilter;
import dev.anhcraft.enc.filters.entity.ShopkeeperFilter;
import org.bukkit.entity.Entity;

public class FilterAssistant {
    private static final Multimap<Class<?>, ObjectFilter<?>> FILTERS = new HashMultimap<>();

    static {
        FILTERS.put(Entity.class, new NPCFilter());
        FILTERS.put(Entity.class, new ShopkeeperFilter());
    }

    @SuppressWarnings("unchecked")
    public static <T> boolean anyMatch(T object, Class<?> clazz){
        var f = FILTERS.get(clazz);
        for(var of : f){
            var of_ = (ObjectFilter<T>) of;
            if(of_.check(object)) return true;
        }
        return false;
    }
}
