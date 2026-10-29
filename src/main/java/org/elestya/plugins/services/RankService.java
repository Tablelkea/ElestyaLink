
/*package org.elestya.plugins.services;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.elestya.plugins.database.Database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.Objects;

public class RankService {

    private final Database database;
    private final Plugin plugin;

    public RankService(Database database, Plugin plugin) {
        this.database = Objects.requireNonNull(database);
        this.plugin = Objects.requireNonNull(plugin);

        createRankHistoryTable();
    }

    private void createRankHistoryTable() {
        String sql = """
            CREATE TABLE IF NOT EXISTS players_rank_history (
                id BIGINT AUTO_INCREMENT PRIMARY KEY,
                uuid VARCHAR(36) NOT NULL,
                `rank` VARCHAR(64) NOT NULL,
                CONSTRAINT fk_rank_history_player
                    FOREIGN KEY (uuid) REFERENCES players(uuid)
                    ON DELETE CASCADE
                    ON UPDATE CASCADE
            )
            """;

        try (Connection c = database.connection();
             Statement st = c.createStatement()) {
            st.executeUpdate(sql);
        } catch (Exception e) {
            plugin.getLogger().severe(
                    "Erreur SQL (createRankHistoryTable) : " + e.getMessage()
            );
        }
    }
    public void setRank(Player player, Rank rank) {
        Objects.requireNonNull(player, "player ne peut pas être null");
        Objects.requireNonNull(rank, "rank ne peut pas être null");

        String sql = """
            INSERT INTO players_rank_history (uuid, `rank`)
            VALUES (?, ?)
            """;

        try (Connection c = database.connection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, player.getUniqueId().toString());
            ps.setString(2, rank.name());

            ps.executeUpdate();
        } catch (Exception e) {
            plugin.getLogger().severe(
                    "Erreur SQL (setRank) : " + e.getMessage()
            );
        }
    }
}*/
