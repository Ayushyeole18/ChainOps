package com.supplychainx.config;

import java.io.InputStream;
import java.util.Properties;

/**
 * Loads database configuration from classpath properties file
 * or environment variables with resilient fallback defaults.
 */
public class DatabaseConfig {

    private static final String DEFAULT_URL = "jdbc:mysql://localhost:3306/supplychainx?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&characterEncoding=UTF-8";
    private static final String DEFAULT_USER = "root";
    private static final String DEFAULT_PASS = "root123";

    private final String url;
    private final String username;
    private final String password;
    private final int maxPoolSize;
    private final int minIdle;
    private final long timeoutMs;

    private static DatabaseConfig instance;

    private DatabaseConfig() {
        Properties props = new Properties();
        try (InputStream in = getClass().getResourceAsStream("/config/db.properties")) {
            if (in != null) {
                props.load(in);
            }
        } catch (Exception ignored) {
            // Fall back to environment variables or defaults
        }

        this.url = System.getenv().getOrDefault("DB_URL", props.getProperty("db.url", DEFAULT_URL));
        this.username = System.getenv().getOrDefault("DB_USER", props.getProperty("db.username", DEFAULT_USER));
        this.password = System.getenv().getOrDefault("DB_PASSWORD", props.getProperty("db.password", DEFAULT_PASS));

        int pool = 10;
        int idle = 2;
        long timeout = 30000;
        try {
            pool = Integer.parseInt(props.getProperty("db.pool.maxSize", "10"));
            idle = Integer.parseInt(props.getProperty("db.pool.minIdle", "2"));
            timeout = Long.parseLong(props.getProperty("db.pool.timeout", "30000"));
        } catch (NumberFormatException ignored) {}

        this.maxPoolSize = pool;
        this.minIdle = idle;
        this.timeoutMs = timeout;
    }

    public static synchronized DatabaseConfig getInstance() {
        if (instance == null) {
            instance = new DatabaseConfig();
        }
        return instance;
    }

    public String getUrl() { return url; }
    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public int getMaxPoolSize() { return maxPoolSize; }
    public int getMinIdle() { return minIdle; }
    public long getTimeoutMs() { return timeoutMs; }
}
