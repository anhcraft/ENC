package dev.anhcraft.enc.utils;

import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class PlayerMap<T> extends HashMap<Player, T> {
    public static List<PlayerMap<?>> instances = new ArrayList<>();

    public PlayerMap() {
        instances.add(this);
    }
}
