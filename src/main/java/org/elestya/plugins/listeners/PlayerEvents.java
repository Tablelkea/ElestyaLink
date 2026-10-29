package org.elestya.plugins.listeners;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;
import org.elestya.plugins.services.PlayerService;
import org.elestya.plugins.services.SanctionService;
import org.elestya.plugins.services.SanctionType;

import java.util.Objects;

public class PlayerEvents implements Listener {

    private final PlayerService playerService;
    private final SanctionService sanctionService;
    private final Plugin plugin;

    public PlayerEvents(PlayerService playerService, SanctionService sanctionService, Plugin plugin) {
        this.playerService = Objects.requireNonNull(playerService);
        this.plugin = Objects.requireNonNull(plugin);
        this.sanctionService = Objects.requireNonNull(sanctionService);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event){
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () ->{
                    playerService.sendPlayerStatus(event.getPlayer(), true);
                }
        );
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event){
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> playerService.sendPlayerStatus(event.getPlayer(), false));
    }

}
