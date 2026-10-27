/*package org.elestya.plugins.services;

import fr.elestya.core.ElestyaCore;
import org.bukkit.entity.Player;
import org.elestya.plugins.database.Database;

import java.sql.Connection;
import java.sql.PreparedStatement;

public class PlayerEconomyHistoryService {

    private Database database;

    public PlayerEconomyHistoryService(Database database) {
        this.database = database;

        createPlayerStatusTable();
    }

    private void createPlayerStatusTable() {
        String sql = "CREATE TABLE IF NOT EXISTS players_economy_history (" +
                "uuid VARCHAR(36) NOT NULL, " +
                "amount INTEGER NOT NULL, " +
                "type VARCHAR(32) NOT NULL, " +
                "timestamp TIMESTAMP NOT NULL)";

        try (Connection c = database.connection(); PreparedStatement st = c.prepareStatement(sql)) {
            st.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void updateEconomyHistory(Player player, double amount){

        String sql = "INSERT INTO players_economy_history (uuid, amount, type, timestamp) "
                + "VALUES (?, ?, ?, CURRENT_TIMESTAMP)";

        try (Connection c = database.connection(); PreparedStatement st = c.prepareStatement(sql)) {
            st.setString(1, player.getUniqueId().toString());
            double money = new ElestyaCore().getEconomyManager().get(player.getUniqueId());
            st.setDouble(2, money);

            if(money - amount >= 0){
                st.setDouble(3, amount);
                st.setString(4, "withdraw");
            }else{
                st.setDouble(3, amount);
                st.setString(4, "deposit");
            }

            st.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}*/
