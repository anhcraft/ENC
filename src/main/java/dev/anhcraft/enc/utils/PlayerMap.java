package dev.anhcraft.enc.utils;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import java.util.*;
import java.util.stream.Stream;

public class PlayerMap<T> extends HashMap<Player, T> {
    public static List<PlayerMap<?>> instances = new ArrayList<>();

    public PlayerMap() {
        instances.add(this);
        Stream<Entity> x= null;
    }
}
