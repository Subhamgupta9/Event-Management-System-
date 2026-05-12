package com.ems.util;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public final class DBConnection {
    private static final Properties PROPERTIES = new Properties();
    private static final String ENV_DB_URL = "EMS_DB_URL";
    private static final String ENV_DB_USERNAME = "EMS_DB_USERNAME";
    private static final String ENV_DB_PASSWORD = "EMS_DB_PASSWORD";
    private static final String PASSWORD_PLACEHOLDER = "your_mysql_password";

    static {
        try (InputStream inputStream = DBConnection.class.getClassLoader().getResourceAsStream("db.properties")) {
            if (inputStream == null) {
                throw new IllegalStateException("db.properties file not found in classpath.");
            }

            PROPERTIES.load(inputStream);
            Class.forName(PROPERTIES.getProperty("db.driver"));
        } catch (IOException | ClassNotFoundException exception) {
            throw new ExceptionInInitializerError("Unable to load database configuration: " + exception.getMessage());
        }
    }

    private DBConnection() {
    }

    // Centralized JDBC connection creation keeps database settings in one place.
    public static Connection getConnection() throws SQLException {
        String url = resolve("db.url", ENV_DB_URL);
        String username = resolve("db.username", ENV_DB_USERNAME);
        String password = resolve("db.password", ENV_DB_PASSWORD);

        validateConfiguration(url, username, password);

        return DriverManager.getConnection(url, username, password == null ? "" : password);
    }

    private static String resolve(String propertyKey, String envKey) {
        String envValue = System.getenv(envKey);
        if (envValue != null && !envValue.isBlank()) {
            return envValue.trim();
        }

        String propertyValue = PROPERTIES.getProperty(propertyKey);
        return propertyValue == null ? null : propertyValue.trim();
    }

    private static void validateConfiguration(String url, String username, String password) throws SQLException {
        if (url == null || url.isBlank()) {
            throw new SQLException("Database URL is missing. Set db.url in db.properties or EMS_DB_URL.");
        }

        if (username == null || username.isBlank()) {
            throw new SQLException("Database username is missing. Set db.username in db.properties or EMS_DB_USERNAME.");
        }

        if (PASSWORD_PLACEHOLDER.equals(password)) {
            throw new SQLException(
                    "Database password is still set to the placeholder value. Update src/main/resources/db.properties or set EMS_DB_PASSWORD."
            );
        }
    }
}
