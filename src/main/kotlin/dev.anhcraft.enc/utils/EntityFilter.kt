package dev.anhcraft.enc.utils

import org.bukkit.entity.Entity
import org.bukkit.entity.LivingEntity

import java.util.function.Predicate

object EntityFilter {
    val NOT_NPC = Predicate<Entity>{entity -> !entity.hasMetadata("NPC")}
    val NOT_SHOPKEEPER = Predicate<Entity>{ entity -> !entity.hasMetadata("shopkeeper") }
    val IS_LIVING_ENTITY = Predicate<Entity>{ entity -> entity is LivingEntity }

    @JvmStatic
    fun check(e : Entity): Boolean{
        return NOT_NPC.and(NOT_SHOPKEEPER).and(IS_LIVING_ENTITY).test(e)
    }
}
