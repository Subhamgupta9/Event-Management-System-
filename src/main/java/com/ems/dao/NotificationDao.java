package com.ems.dao;

import com.ems.model.Notification;
import com.ems.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class NotificationDao {
    private static final String CREATE_NOTIFICATIONS_TABLE_SQL = """
            CREATE TABLE IF NOT EXISTS notifications (
                id INT PRIMARY KEY AUTO_INCREMENT,
                user_id INT NOT NULL,
                message VARCHAR(255) NOT NULL,
                link_url VARCHAR(255) NULL,
                is_read BOOLEAN NOT NULL DEFAULT FALSE,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                CONSTRAINT fk_notifications_user
                    FOREIGN KEY (user_id) REFERENCES users(id)
                    ON DELETE CASCADE
            )
            """;

    public boolean createNotification(int userId, String message, String linkUrl) throws SQLException {
        String sql = "INSERT INTO notifications (user_id, message, link_url) VALUES (?, ?, ?)";

        try (Connection connection = DBConnection.getConnection()) {
            ensureNotificationSchema(connection);

            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                preparedStatement.setInt(1, userId);
                preparedStatement.setString(2, message);
                preparedStatement.setString(3, linkUrl);
                return preparedStatement.executeUpdate() > 0;
            }
        }
    }

    public List<Notification> getNotificationsForUser(int userId) throws SQLException {
        List<Notification> notifications = new ArrayList<>();
        String sql = "SELECT id, user_id, message, link_url, is_read, created_at FROM notifications WHERE user_id = ? ORDER BY created_at DESC LIMIT 10";

        try (Connection connection = DBConnection.getConnection()) {
            ensureNotificationSchema(connection);

            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                preparedStatement.setInt(1, userId);
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    while (resultSet.next()) {
                        Notification notification = new Notification();
                        notification.setId(resultSet.getInt("id"));
                        notification.setUserId(resultSet.getInt("user_id"));
                        notification.setMessage(resultSet.getString("message"));
                        notification.setLinkUrl(resultSet.getString("link_url"));
                        notification.setRead(resultSet.getBoolean("is_read"));
                        notification.setCreatedAt(resultSet.getTimestamp("created_at"));
                        notifications.add(notification);
                    }
                }
            }
        }

        return notifications;
    }

    public int countUnreadNotifications(int userId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM notifications WHERE user_id = ? AND is_read = FALSE";
        try (Connection connection = DBConnection.getConnection()) {
            ensureNotificationSchema(connection);

            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                preparedStatement.setInt(1, userId);
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    resultSet.next();
                    return resultSet.getInt(1);
                }
            }
        }
    }

    public boolean markAllAsRead(int userId) throws SQLException {
        String sql = "UPDATE notifications SET is_read = TRUE WHERE user_id = ?";
        try (Connection connection = DBConnection.getConnection()) {
            ensureNotificationSchema(connection);

            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                preparedStatement.setInt(1, userId);
                return preparedStatement.executeUpdate() >= 0;
            }
        }
    }

    public boolean clearAllNotifications(int userId) throws SQLException {
        String sql = "DELETE FROM notifications WHERE user_id = ?";
        try (Connection connection = DBConnection.getConnection()) {
            ensureNotificationSchema(connection);

            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                preparedStatement.setInt(1, userId);
                return preparedStatement.executeUpdate() >= 0;
            }
        }
    }

    private void ensureNotificationSchema(Connection connection) throws SQLException {
        try (PreparedStatement preparedStatement = connection.prepareStatement(CREATE_NOTIFICATIONS_TABLE_SQL)) {
            preparedStatement.executeUpdate();
        }
    }
}
