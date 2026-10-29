/*package org.elestya.plugins.services;

import org.elestya.plugins.database.Database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class JobService {

    public enum Job {
        MINEUR, BUCHERON, PECHEUR, FERMIER, CONSTRUCTEUR, EXPLORATEUR
    }

    public record JobData(int level, int prestige) {
        public static JobData defaults() {
            return new JobData(1, 0);
        }
    }

    private final Database database;
    private final JobManager jobManager;

    public JobService(Database database, JobManager jobManager) {
        this.database = database;
        this.jobManager = jobManager;

        createJobTable();
    }

    private void createJobTable() {
        String sql = """
            CREATE TABLE IF NOT EXISTS players_jobs (
                uuid VARCHAR(36) NOT NULL,
                metier VARCHAR(32) NOT NULL,
                level INT NOT NULL DEFAULT 1,
                prestige INT NOT NULL DEFAULT 0,
                PRIMARY KEY (uuid, metier)
            )""";

        try (Connection c = database.connection(); Statement st = c.createStatement()) {
            st.executeUpdate(sql);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public Map<Job, JobData> loadJobs(UUID uuid) {
        Objects.requireNonNull(uuid);

        Map<Job, JobData> jobs = new EnumMap<>(Job.class);
        for (Job job : Job.values()) {
            jobs.put(job, JobData.defaults());
        }

        String sql = "SELECT metier, level, prestige FROM players_jobs WHERE uuid = ?";

        try (Connection c = database.connection(); PreparedStatement st = c.prepareStatement(sql)) {
            st.setString(1, uuid.toString());

            try (ResultSet rs = st.executeQuery()) {
                while (rs.next()) {
                    try {
                        Job job = Job.valueOf(rs.getString("metier"));
                        jobs.put(job, new JobData(rs.getInt("level"), rs.getInt("prestige")));
                    } catch (IllegalArgumentException ignored) {
                        // Métier inconnu en base (ancien nom, etc.) : ignoré.
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return jobs;
    }

    public JobData loadJob(UUID uuid, Job job) {
        Objects.requireNonNull(uuid);
        Objects.requireNonNull(job);

        String sql = "SELECT level, prestige FROM players_jobs WHERE uuid = ? AND metier = ?";

        try (Connection c = database.connection(); PreparedStatement st = c.prepareStatement(sql)) {
            st.setString(1, uuid.toString());
            st.setString(2, job.name());

            try (ResultSet rs = st.executeQuery()) {
                if (rs.next()) {
                    return new JobData(rs.getInt("level"), rs.getInt("prestige"));
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return JobData.defaults();
    }

    public void saveJob(UUID uuid, Job job, int level, int prestige) {
        Objects.requireNonNull(uuid);
        Objects.requireNonNull(job);

        String sql = """
            INSERT INTO players_jobs (uuid, metier, level, prestige) VALUES (?, ?, ?, ?)
            ON DUPLICATE KEY UPDATE level = VALUES(level), prestige = VALUES(prestige)""";

        try (Connection c = database.connection(); PreparedStatement st = c.prepareStatement(sql)) {
            st.setString(1, uuid.toString());
            st.setString(2, job.name());
            st.setInt(3, level);
            st.setInt(4, prestige);
            st.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void saveJobs(UUID uuid, Map<Job, JobData> jobs) {
        Objects.requireNonNull(uuid);
        Objects.requireNonNull(jobs);

        if (jobs.isEmpty()) return;

        String sql = """
            INSERT INTO players_jobs (uuid, metier, level, prestige) VALUES (?, ?, ?, ?)
            ON DUPLICATE KEY UPDATE level = VALUES(level), prestige = VALUES(prestige)""";

        try (Connection c = database.connection()) {
            c.setAutoCommit(false);

            try (PreparedStatement st = c.prepareStatement(sql)) {
                for (Map.Entry<Job, JobData> entry : jobs.entrySet()) {
                    st.setString(1, uuid.toString());
                    st.setString(2, entry.getKey().name());
                    st.setInt(3, entry.getValue().level());
                    st.setInt(4, entry.getValue().prestige());
                    st.addBatch();
                }

                st.executeBatch();
                c.commit();
            } catch (Exception e) {
                c.rollback();
                throw e;
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}*/