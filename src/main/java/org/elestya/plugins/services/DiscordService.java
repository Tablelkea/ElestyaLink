package org.elestya.plugins.services;

import org.bukkit.entity.Player;
import org.elestya.plugins.database.Database;

import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class DiscordService {

    // Sans 0/O/1/I pour éviter les confusions à la saisie
    private static final String ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final int CODE_LENGTH = 6;
    private static final int MAX_ATTEMPTS = 5;
    private static final long CODE_TTL_MS = 10 * 60 * 1000L; // 10 minutes
    private static final SecureRandom RANDOM = new SecureRandom();

    private final Database database;
    // Joueurs qui ont demandé un code et attendent de le valider sur Discord (uuid -> expiration)
    private final Map<UUID, Long> pending = new ConcurrentHashMap<>();

    public DiscordService(Database database) {
        this.database = Objects.requireNonNull(database);
        createTables();
    }

    private void createTables() {
        // Codes en attente (écrits par le plugin, lus et supprimés par le bot)
        String links = "CREATE TABLE IF NOT EXISTS discord_links ("
                + "unique_id VARCHAR(36) PRIMARY KEY,"
                + "code VARCHAR(6) NOT NULL,"
                + "expiration_time BIGINT NOT NULL,"
                + "UNIQUE KEY uk_discord_links_code (code)"
                + ")";
        // Liaisons validées (écrites par le bot, lues par le plugin)
        String accounts = "CREATE TABLE IF NOT EXISTS discord_accounts ("
                + "discord_id VARCHAR(20) PRIMARY KEY,"
                + "unique_id VARCHAR(36) NOT NULL UNIQUE,"
                + "mc_name VARCHAR(16),"
                + "linked_at BIGINT NOT NULL"
                + ")";
        try (Connection c = database.connection()) {
            for (String sql : new String[]{links, accounts}) {
                try (PreparedStatement st = c.prepareStatement(sql)) {
                    st.executeUpdate();
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /** À appeler hors du thread principal (accès base de données). */
    public void link(Player player) {
        if (isLinked(player.getUniqueId())) {
            player.sendMessage("§eTon compte est déjà lié. Utilise §6/unlink §esur Discord pour le délier.");
            return;
        }
        // On génère et on enregistre AVANT d'afficher : le joueur ne voit jamais un code qui n'existe pas
        String code = createPendingLink(player.getUniqueId());
        pending.put(player.getUniqueId(), System.currentTimeMillis() + CODE_TTL_MS);
        player.sendMessage("§aTon code : §e" + code + " §a→ tape §e/link code:" + code + " §asur Discord");
        player.sendMessage("§7Valable " + (CODE_TTL_MS / 60_000) + " minutes.");
    }

    /**
     * Retourne les joueurs qui viennent de valider leur code sur Discord.
     * À appeler périodiquement HORS du thread principal. La base n'est interrogée
     * que s'il y a des liaisons en attente.
     */
    public Set<UUID> pollLinked() {
        long now = System.currentTimeMillis();
        pending.values().removeIf(expiry -> expiry < now); // code expiré : on arrête d'attendre
        if (pending.isEmpty()) {
            return Set.of();
        }

        List<UUID> waiting = new ArrayList<>(pending.keySet());
        String placeholders = String.join(",", Collections.nCopies(waiting.size(), "?"));
        String sql = "SELECT unique_id FROM discord_accounts WHERE unique_id IN (" + placeholders + ")";

        Set<UUID> linked = new HashSet<>();
        try (Connection c = database.connection(); PreparedStatement st = c.prepareStatement(sql)) {
            for (int i = 0; i < waiting.size(); i++) {
                st.setString(i + 1, waiting.get(i).toString());
            }
            try (ResultSet rs = st.executeQuery()) {
                while (rs.next()) {
                    linked.add(UUID.fromString(rs.getString(1)));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        linked.forEach(pending::remove);
        return linked;
    }

    /** Message envoyé au joueur quand sa liaison est validée (thread principal). */
    public void notifyLinked(Player player) {
        player.sendMessage("§a✔ Ton compte Minecraft est maintenant lié à Discord !");
    }

    public boolean isLinked(UUID uniqueId) {
        String sql = "SELECT 1 FROM discord_accounts WHERE unique_id = ?";
        try (Connection c = database.connection(); PreparedStatement st = c.prepareStatement(sql)) {
            st.setString(1, uniqueId.toString());
            try (ResultSet rs = st.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Remplace l'éventuel code précédent du joueur, nettoie les codes expirés,
     * puis insère un nouveau code unique (réessaie en cas de collision).
     */
    private String createPendingLink(UUID uniqueId) {
        try (Connection c = database.connection()) {
            String cleanup = "DELETE FROM discord_links WHERE unique_id = ? OR expiration_time < ?";
            try (PreparedStatement st = c.prepareStatement(cleanup)) {
                st.setString(1, uniqueId.toString());
                st.setLong(2, System.currentTimeMillis());
                st.executeUpdate();
            }

            String insert = "INSERT INTO discord_links (unique_id, code, expiration_time) VALUES (?, ?, ?)";
            for (int attempt = 0; attempt < MAX_ATTEMPTS; attempt++) {
                String code = generateCode();
                try (PreparedStatement st = c.prepareStatement(insert)) {
                    st.setString(1, uniqueId.toString());
                    st.setString(2, code);
                    st.setLong(3, System.currentTimeMillis() + CODE_TTL_MS);
                    st.executeUpdate();
                    return code;
                } catch (SQLIntegrityConstraintViolationException duplicate) {
                    // Code déjà utilisé par un autre joueur : on retire un autre
                }
            }
            throw new IllegalStateException("Impossible de générer un code unique");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private static String generateCode() {
        StringBuilder sb = new StringBuilder(CODE_LENGTH);
        for (int i = 0; i < CODE_LENGTH; i++) {
            sb.append(ALPHABET.charAt(RANDOM.nextInt(ALPHABET.length())));
        }
        return sb.toString();
    }
}