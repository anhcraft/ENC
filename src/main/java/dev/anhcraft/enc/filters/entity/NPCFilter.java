package dev.anhcraft.enc.filters.entity;

import dev.anhcraft.enc.filters.ObjectFilter;
import org.bukkit.entity.Entity;

public class NPCFilter extends ObjectFilter<Entity> {
    @Override
    public boolean check(Entity object) {
        return object.hasMetadata("NPC");
    }
}
