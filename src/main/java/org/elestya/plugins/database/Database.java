package org.elestya.plugins.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;

public class Database {

    private HikariDataSource dataSource;

    public void connect(
            String host,
            int port,
            String db,
            String user,
            String password
    ) {
        HikariConfig dbconfig = new HikariConfig();

        dbconfig.setUsername(user);
        dbconfig.setPassword(password);
        dbconfig.setJdbcUrl("jdbc:mysql://" + host + ":" + port + "/" + db);
        dbconfig.setMaximumPoolSize(10);
        dbconfig.setPoolName("Elestya-pool");
        this.dataSource = new HikariDataSource(dbconfig);
    }

    public Connection connection() throws SQLException {
        return this.dataSource.getConnection();
    }

    public void close() {
        if(dataSource != null && !dataSource.isClosed())
            this.dataSource.close();
    }
}
