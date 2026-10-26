package org.elestya.plugins;

import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.elestya.plugins.database.Database;
import org.elestya.plugins.listeners.PlayerEvents;
import org.elestya.plugins.listeners.PlayerSessionListener;
import org.elestya.plugins.services.PlayerService;
import org.elestya.plugins.services.PlayerSessionService;
import org.elestya.plugins.services.SanctionService;

public final class ElestyaLink extends JavaPlugin {

    private Database database;
    private PlayerSessionService sessionService;

    @Override
    public void onEnable() {

        saveDefaultConfig();
        database = new Database();
        try {
            database.connect(
                    getConfig().getString("connection.host"),
                    getConfig().getInt("connection.port"),
                    getConfig().getString("connection.db"),
                    getConfig().getString("connection.username"),
                    getConfig().getString("connection.password")
            );

            PlayerService playerService = new PlayerService(database, this);
            SanctionService sanctionService = new SanctionService(database, this);
            sessionService = new PlayerSessionService(database, this);

            PluginManager pm = getServer().getPluginManager();
            pm.registerEvents(new PlayerEvents(playerService, sanctionService, this), this);
            pm.registerEvents(new PlayerSessionListener(this, sessionService), this);

        } catch (Exception e){
            getLogger().severe("Failed to connect to the database: " + e.getMessage());
            getServer().getPluginManager().disablePlugin(this);
        }

    }

    @Override
    public void onDisable() {
        if (sessionService != null) {
            sessionService.shutdown();
        }
        if (database != null) {
            database.close();
        }
    }
}