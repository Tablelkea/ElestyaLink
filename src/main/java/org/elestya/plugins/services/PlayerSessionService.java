
package org.elestya.plugins.services;

import org.elestya.plugins.database.Database;
import org.bukkit.plugin.Plugin;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class PlayerSessionService {

    private final Database database;
    private final Plugin plugin;
    private final ExecutorService executor;

    public PlayerSessionService(Database database, Plugin plugin) {
        this.database = Objects.requireNonNull(database);
        this.plugin = Objects.requireNonNull(plugin);
        this.executor = Executors.newSingleThreadExecutor(runnable -> {
            Thread thread = new Thread(runnable, "ElestyaLink-Sessions");
            thread.setDaemon(true);
            return thread;
        });

        createTable();
        recoverOpenSessions();
    }

    private void createTable() {
        String sql = """
            CREATE TABLE IF NOT EXISTS player_sessions (
                id BIGINT AUTO_INCREMENT PRIMARY KEY,
                uuid VARCHAR(36) NOT NULL,
                player_name VARCHAR(16) NOT NULL,
                joined_at DATETIME NOT NULL,
                left_at DATETIME NULL,
                last_heartbeat DATETIME NOT NULL,
                duration_seconds BIGINT NOT NULL DEFAULT 0,
                INDEX idx_sessions_uuid (uuid),
                INDEX idx_sessions_joined (joined_at)
            )
            """;

        try (Connection connection = database.connection();
             Statement statement = connection.createStatement()) {
            statement.executeUpdate(sql);
        } catch (SQLException e) {
            throw new RuntimeException(
                    "Impossible de créer la table player_sessions", e
            );
        }
    }

    public void recordJoin(UUID uuid, String playerName) {
        Objects.requireNonNull(uuid);
        Objects.requireNonNull(playerName);

        executor.execute(() -> {
            String sql = """
                INSERT INTO player_sessions
                    (uuid, player_name, joined_at, last_heartbeat)
                VALUES (?, ?, NOW(), NOW())
                """;

            try (Connection connection = database.connection();
                 PreparedStatement statement =
                         connection.prepareStatement(sql)) {
                statement.setString(1, uuid.toString());
                statement.setString(2, playerName);
                statement.executeUpdate();
            } catch (SQLException e) {
                logError("enregistrement de connexion", e);
            }
        });
    }

    public void recordQuit(UUID uuid) {
        Objects.requireNonNull(uuid);

        executor.execute(() -> {
            String sql = """
                UPDATE player_sessions
                SET left_at = NOW(),
                    last_heartbeat = NOW(),
                    duration_seconds =
                        TIMESTAMPDIFF(SECOND, joined_at, NOW())
                WHERE uuid = ?
                  AND left_at IS NULL
                ORDER BY joined_at DESC
                LIMIT 1
                """;

            try (Connection connection = database.connection();
                 PreparedStatement statement =
                         connection.prepareStatement(sql)) {
                statement.setString(1, uuid.toString());
                statement.executeUpdate();
            } catch (SQLException e) {
                logError("enregistrement de déconnexion", e);
            }
        });
    }

    public void refreshOnlineSessions() {
        executor.execute(() -> {
            String sql = """
                UPDATE player_sessions
                SET last_heartbeat = NOW(),
                    duration_seconds =
                        TIMESTAMPDIFF(SECOND, joined_at, NOW())
                WHERE left_at IS NULL
                """;

            try (Connection connection = database.connection();
                 PreparedStatement statement =
                         connection.prepareStatement(sql)) {
                statement.executeUpdate();
            } catch (SQLException e) {
                logError("actualisation des sessions", e);
            }
        });
    }

    private void recoverOpenSessions() {
        String sql = """
            UPDATE player_sessions
            SET left_at = last_heartbeat,
                duration_seconds =
                    GREATEST(
                        0,
                        TIMESTAMPDIFF(
                            SECOND, joined_at, last_heartbeat
                        )
                    )
            WHERE left_at IS NULL
            """;

        try (Connection connection = database.connection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            int recovered = statement.executeUpdate();

            if (recovered > 0) {
                plugin.getLogger().info(
                        recovered + " session(s) interrompue(s) récupérée(s)."
                );
            }
        } catch (SQLException e) {
            throw new RuntimeException(
                    "Impossible de récupérer les sessions ouvertes", e
            );
        }
    }

    private void logError(String operation, SQLException e) {
        plugin.getLogger().severe(
                "Erreur SQL pendant " + operation + " : " + e.getMessage()
        );
    }

    public void shutdown() {
        executor.shutdown();

        try {
            if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                plugin.getLogger().warning(
                        "Certaines opérations de sessions sont encore en attente."
                );
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
