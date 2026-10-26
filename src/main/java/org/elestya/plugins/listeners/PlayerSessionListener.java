
package org.elestya.plugins.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;
import org.elestya.plugins.services.PlayerSessionService;

import java.util.Objects;

public class PlayerSessionListener implements Listener {

    private final PlayerSessionService sessionService;

    public PlayerSessionListener(
            Plugin plugin,
            PlayerSessionService sessionService
    ) {
        Objects.requireNonNull(plugin);
        this.sessionService = Objects.requireNonNull(sessionService);

        // Actualise la durée des sessions chaque minute.
        plugin.getServer().getScheduler().runTaskTimer(
                plugin,
                sessionService::refreshOnlineSessions,
                1200L,
                1200L
        );
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        sessionService.recordJoin(
                event.getPlayer().getUniqueId(),
                event.getPlayer().getName()
        );
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        sessionService.recordQuit(event.getPlayer().getUniqueId());
    }
}
