package org.anhcraft.enc.utils;

import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

public class DelayedRunnable {
    private Plugin plugin;
    private Runnable runnable;
    private long duration;
    private boolean async;
    private boolean inQueue;

    public DelayedRunnable(Plugin plugin, Runnable runnable, long duration, boolean async){
        this.runnable = runnable;
        this.plugin = plugin;
        this.duration = duration;
        this.async = async;
        inQueue = false;
    }

    public synchronized void run(){
        if(!inQueue){
            inQueue = true;
            if(async){
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        inQueue = false;
                        runnable.run();
                    }
                }.runTaskLaterAsynchronously(plugin, duration);
            } else {
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        inQueue = false;
                        runnable.run();
                    }
                }.runTaskLater(plugin, duration);
            }
        }
    }

    public void setAsync(boolean async) {
        this.async = async;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }
}
