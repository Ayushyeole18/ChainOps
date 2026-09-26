package com.supplychainx.config;

import com.supplychainx.exception.DatabaseException;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Manages JDBC connections using the HikariCP connection pool.
 * Provides transaction helpers for atomic multi-table business operations.
 */
public class DatabaseConnection {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseConnection.class);
    private static HikariDataSource dataSource;

    static {
        try {
            DatabaseConfig config = DatabaseConfig.getInstance();
            HikariConfig hikariConfig = new HikariConfig();
            hikariConfig.setJdbcUrl(config.getUrl());
            hikariConfig.setUsername(config.getUsername());
            hikariConfig.setPassword(config.getPassword());
            hikariConfig.setMaximumPoolSize(config.getMaxPoolSize());
            hikariConfig.setMinimumIdle(config.getMinIdle());
            hikariConfig.setConnectionTimeout(config.getTimeoutMs());
            hikariConfig.setDriverClassName("com.mysql.cj.jdbc.Driver");
            hikariConfig.setPoolName("ChainOpsHikariPool");

            // Optimizations
            hikariConfig.addDataSourceProperty("cachePrepStmts", "true");
            hikariConfig.addDataSourceProperty("prepStmtCacheSize", "250");
            hikariConfig.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
            hikariConfig.addDataSourceProperty("useServerPrepStmts", "true");

            dataSource = new HikariDataSource(hikariConfig);
            logger.info("HikariCP initialized successfully for database: {}", config.getUrl());
        } catch (Exception e) {
            logger.error("Failed to initialize database connection pool", e);
            // Do not terminate static init so application can display user-friendly connection error
        }
    }

    private DatabaseConnection() {}

    /**
     * Obtains an active connection from the pool.
     */
    public static Connection getConnection() throws SQLException {
        if (dataSource == null || dataSource.isClosed()) {
            throw new SQLException("Database connection pool is not initialized or closed.");
        }
        return dataSource.getConnection();
    }

    /**
     * Checks if the database is reachable.
     */
    public static boolean testConnection() {
        try (Connection conn = getConnection()) {
            return conn != null && !conn.isClosed();
        } catch (Exception e) {
            logger.warn("Database test connection failed: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Safely rolls back a transaction.
     */
    public static void rollbackQuietly(Connection conn) {
        if (conn != null) {
            try {
                conn.rollback();
            } catch (SQLException e) {
                logger.error("Failed to rollback transaction", e);
            }
        }
    }

    /**
     * Closes the connection pool on application shutdown.
     */
    public static void shutdown() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            logger.info("HikariCP pool shut down successfully.");
        }
    }
}
