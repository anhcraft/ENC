package dev.anhcraft.enc.listeners;

import dev.anhcraft.enc.utils.PlayerMap;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerListener implements Listener {
    @EventHandler
    public void cleanPlayers(PlayerQuitEvent event){
        PlayerMap.instances.forEach(e -> e.remove(event.getPlayer()));
    }
}
