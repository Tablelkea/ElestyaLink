package org.elestya.plugins.services;

import org.bukkit.plugin.Plugin;
import org.elestya.plugins.database.Database;

import java.sql.*;
import java.util.*;

public class SanctionService {

    private final Database database;
    private final Plugin plugin;

    public SanctionService(Database database, Plugin plugin) {
        this.database = Objects.requireNonNull(database);
        this.plugin = Objects.requireNonNull(plugin);

        createSanctionsTable();
    }

    private void createSanctionsTable() {
        String sql = """
            CREATE TABLE IF NOT EXISTS sanctions (
                id INT AUTO_INCREMENT PRIMARY KEY,
                uuid VARCHAR(36) NOT NULL,
                type VARCHAR(16) NOT NULL,
                reason VARCHAR(255) NOT NULL,
                staff VARCHAR(36) NOT NULL,
                created_at BIGINT NOT NULL,
                expires_at BIGINT NULL,
                active BOOLEAN NOT NULL DEFAULT TRUE,
                INDEX idx_sanctions_uuid (uuid)
            )""";
        try (Connection c = database.connection(); Statement st = c.createStatement()) {
            st.executeUpdate(sql);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Ajoute une sanction. durationMillis = null pour une sanction permanente.
     * À appeler hors du thread principal.
     */
    public void addSanction(UUID target, String reason, SanctionType type, String staff, Long durationMillis){
        String sql = "INSERT INTO sanctions (uuid, type, reason, staff, created_at, expires_at) "
                + "VALUES (?, ?, ?, ?, ?, ?)";
        long now = System.currentTimeMillis();

        try (Connection c = database.connection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, target.toString());
            ps.setString(2, type.name());
            ps.setString(3, reason);
            ps.setString(4, staff);
            ps.setLong(5, now);
            if (durationMillis == null) {
                ps.setNull(6, Types.BIGINT);
            } else {
                ps.setLong(6, now + durationMillis);
            }
            ps.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().severe("Erreur SQL (addSanction) : " + e.getMessage());
        }
    }
}