package org.elestya.plugins.services;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.elestya.plugins.database.Database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Objects;

public class PlayerService {

    private final Database database;
    private final Plugin plugin;

    public PlayerService(Database database, Plugin plugin) {
        this.database = Objects.requireNonNull(database);
        this.plugin = Objects.requireNonNull(plugin);

        createPlayerStatusTable();
    }

    private void createPlayerStatusTable() {
        String sql = """
            CREATE TABLE IF NOT EXISTS players (
                uuid VARCHAR(36) PRIMARY KEY,
                name VARCHAR(16) NOT NULL,
                last_login TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                first_login TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                online BOOLEAN NOT NULL DEFAULT FALSE
            )""";
        try (Connection c = database.connection(); Statement st = c.createStatement()) {
            st.executeUpdate(sql);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /*
     * Sends the player status to the database.
     */
    public void sendPlayerStatus(Player player, boolean online) {
        String sql = "INSERT INTO players (uuid, name, last_login, first_login, online) "
                + "VALUES (?, ?, ?, ?, ?) "
                + "ON DUPLICATE KEY UPDATE name = VALUES(name), "
                + "last_login = VALUES(last_login), online = VALUES(online)";

        Timestamp now = new Timestamp(System.currentTimeMillis());

        try (Connection c = database.connection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, player.getUniqueId().toString());
            ps.setString(2, player.getName());
            ps.setTimestamp(3, now);
            ps.setTimestamp(4, now);
            ps.setBoolean(5, online);
            ps.executeUpdate();
        } catch (Exception e) {
            plugin.getLogger().severe("Erreur SQL (sendPlayerStatus) : " + e.getMessage());
        }
    }
}
