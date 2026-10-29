package org.elestya.plugins;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.elestya.plugins.commands.DiscordLinkCommand;
import org.elestya.plugins.database.Database;
import org.elestya.plugins.listeners.PlayerEvents;
import org.elestya.plugins.listeners.PlayerSessionListener;
import org.elestya.plugins.services.*;

import java.util.Set;
import java.util.UUID;

public final class ElestyaLink extends JavaPlugin {

    private static ElestyaLink instance;

    private Database database;
    private PlayerSessionService sessionService;
    private PlayerService playerService;
    private SanctionService sanctionService;
    //private PlayerEconomyHistoryService playerEconomyHistoryService;
    //private PlayerEconomyService playerEconomyService;
    private DiscordService discordService;

    @Override
    public void onEnable() {

        instance = this;

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

            playerService = new PlayerService(database, this);
            sanctionService = new SanctionService(database, this);
            sessionService = new PlayerSessionService(database, this);
            //playerEconomyHistoryService = new PlayerEconomyHistoryService(database);
            //playerEconomyService = new PlayerEconomyService(database);
            discordService = new DiscordService(database);

            PluginManager pm = getServer().getPluginManager();
            pm.registerEvents(new PlayerEvents(playerService, sanctionService, this), this);
            pm.registerEvents(new PlayerSessionListener(this, sessionService), this);

            getCommand("discordlink").setExecutor(new DiscordLinkCommand(database, discordService));

        } catch (Exception e){
            getLogger().severe("Failed to connect to the database: " + e.getMessage());
            getServer().getPluginManager().disablePlugin(this);
        }

        /*Bukkit.getScheduler().runTaskTimer(this, () -> {

            for(Player player : Bukkit.getOnlinePlayers())
                playerEconomyService.updatePlayerMoney(player);

        }, 0L, 30*20L);*/

        Bukkit.getScheduler().runTaskTimerAsynchronously(this, () -> {
            try {
                Set<UUID> linked = discordService.pollLinked();   // requête base, hors thread principal
                if (linked.isEmpty()) return;

                Bukkit.getScheduler().runTask(this, () -> {       // envoi du message sur le thread principal
                    for (UUID id : linked) {
                        Player p = Bukkit.getPlayer(id);
                        if (p != null) discordService.notifyLinked(p);
                    }
                });
            } catch (Exception e) {
                getLogger().warning("Vérification des liaisons Discord : " + e.getMessage());
            }
        }, 100L, 60L); // première vérif après 5 s, puis toutes les 3 s

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

    public static ElestyaLink instance() {
        return instance;
    }

    public Database database(){
        return database;
    }

    public PlayerSessionService sessionService(){
        return sessionService;
    }

    public SanctionService sanctionService(){
        return sanctionService;
    }

    public PlayerService playerService(){
        return playerService;
    }

    /*public PlayerEconomyHistoryService playerEconomyHistoryService(){
        return playerEconomyHistoryService;
    }*/

    public DiscordService discordService(){
        return discordService;
    }
}