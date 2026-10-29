package org.elestya.plugins.services;

import org.bukkit.entity.Player;
import org.elestya.plugins.database.Database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.Objects;

public class PlayerEconomyHistoryService {

    public static final String DEPOSIT = "deposit";
    public static final String WITHDRAW = "withdraw";

    private final Database database;

    public PlayerEconomyHistoryService(Database database) {
        this.database = database;
        createTable();
    }

    private void createTable() {
        String sql = """
            CREATE TABLE IF NOT EXISTS players_economy_history (
                id BIGINT AUTO_INCREMENT PRIMARY KEY,
                uuid VARCHAR(36) NOT NULL,
                amount DOUBLE NOT NULL,
                type VARCHAR(32) NOT NULL,
                timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                INDEX idx_uuid_timestamp (uuid, timestamp)
            )""";

        try (Connection c = database.connection(); Statement st = c.createStatement()) {
            st.executeUpdate(sql);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Enregistre une transaction.
     *
     * @param player le joueur concerné
     * @param delta  variation du solde : positive = dépôt, négative = retrait
     */
    public void recordTransaction(Player player, double delta) {
        Objects.requireNonNull(player);

        if (delta == 0) return;

        String type = delta > 0 ? DEPOSIT : WITHDRAW;

        String sql = "INSERT INTO players_economy_history (uuid, amount, type) VALUES (?, ?, ?)";

        try (Connection c = database.connection(); PreparedStatement st = c.prepareStatement(sql)) {
            st.setString(1, player.getUniqueId().toString());
            st.setDouble(2, Math.abs(delta));
            st.setString(3, type);
            st.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}