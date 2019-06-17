package dev.anhcraft.enc.utils

import com.google.common.collect.ArrayListMultimap
import org.bukkit.entity.Entity
import org.bukkit.entity.LivingEntity
import java.util.function.Predicate

object FilterAssistant {
    private val FILTERS = ArrayListMultimap.create<Class<*>, Predicate<*>>()

    init {
        FILTERS.put(Entity::class.java, Predicate<Entity> { o -> o.hasMetadata("NPC") })
        FILTERS.put(Entity::class.java, Predicate<Entity> { o -> o.hasMetadata("shopkeeper") })
        FILTERS.put(Entity::class.java, Predicate<Entity> { o -> o !is LivingEntity })
    }

    @JvmStatic
    fun <T> anyMatch(o: T, clazz: Class<T>): Boolean {
        val f = FILTERS.get(clazz)
        for (of in f) {
            if ((of as Predicate<T>).test(o)) return true
        }
        return false
    }
}
