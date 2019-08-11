package dev.anhcraft.enc.utils

import org.bukkit.entity.Player

class PlayerMap<T>() : HashMap<Player, T>() {
    companion object{
        val instances = ArrayList<PlayerMap<*>>()
    }

    init {
        instances.add(this)
    }
}
