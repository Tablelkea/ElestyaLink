/*package org.elestya.plugins.services;

import fr.elestya.core.ElestyaCore;
import org.bukkit.entity.Player;
import org.elestya.plugins.database.Database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.Objects;

public class PlayerEconomyService {

    private Database database;

    public PlayerEconomyService(Database database) {
        this.database = database;

        createPlayerStatusTable();
    }

    private void createPlayerStatusTable() {
        String sql = """
            CREATE TABLE IF NOT EXISTS players_economy (
                uuid VARCHAR(36) PRIMARY KEY,
                name VARCHAR(16) NOT NULL,
                money DOUBLE NOT NULL DEFAULT 0.0
            )""";
        try (Connection c = database.connection(); Statement st = c.createStatement()) {
            st.executeUpdate(sql);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void updatePlayerMoney(Player player){

        Objects.requireNonNull(player);
        double amount = new ElestyaCore().getEconomyManager().get(player.getUniqueId());

        String sql = "UPDATE players_economy SET money = money + ? WHERE uuid = ?";

        try (Connection c = database.connection(); PreparedStatement st = c.prepareStatement(sql)) {
            st.setDouble(1, amount);
            st.setString(2, player.getUniqueId().toString());
            st.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

}*/
