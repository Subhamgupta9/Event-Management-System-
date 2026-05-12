package com.ems.dao;

import com.ems.model.Message;
import com.ems.model.User;
import com.ems.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

public class MessageDao {
    private final EventDao eventDao = new EventDao();

    public boolean saveMessage(Message message) throws SQLException {
        if (message == null || message.getSenderId() <= 0) {
            throw new IllegalArgumentException("Valid sender details are required.");
        }
        if (message.getMessageText() == null || message.getMessageText().isBlank()) {
            throw new IllegalArgumentException("Message cannot be empty.");
        }
        boolean groupMessage = message.getGroupId() != null;
        boolean eventMessage = message.getEventId() != null;
        if (groupMessage == eventMessage) {
            throw new IllegalArgumentException("Message must belong to exactly one chat scope.");
        }

        String sql = "INSERT INTO messages (sender_id, group_id, event_id, message_text) VALUES (?, ?, ?, ?)";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, message.getSenderId());
            if (message.getGroupId() == null) {
                preparedStatement.setNull(2, java.sql.Types.INTEGER);
            } else {
                preparedStatement.setInt(2, message.getGroupId());
            }
            if (message.getEventId() == null) {
                preparedStatement.setNull(3, java.sql.Types.INTEGER);
            } else {
                preparedStatement.setInt(3, message.getEventId());
            }
            preparedStatement.setString(4, message.getMessageText());
            return preparedStatement.executeUpdate() > 0;
        }
    }

    public List<Message> getGroupMessages(int groupId) throws SQLException {
        return getMessages(
                """
                SELECT m.id, m.sender_id, m.group_id, m.event_id, m.message_text, m.created_at, u.full_name
                FROM messages m
                INNER JOIN users u ON m.sender_id = u.id
                WHERE m.group_id = ?
                ORDER BY m.created_at ASC, m.id ASC
                """,
                groupId
        );
    }

    public List<Message> getEventMessages(int eventId) throws SQLException {
        return getMessages(
                """
                SELECT m.id, m.sender_id, m.group_id, m.event_id, m.message_text, m.created_at, u.full_name
                FROM messages m
                INNER JOIN users u ON m.sender_id = u.id
                WHERE m.event_id = ?
                ORDER BY m.created_at ASC, m.id ASC
                """,
                eventId
        );
    }

    public Map<Integer, List<Message>> getEventMessagesForEvents(List<Integer> eventIds) throws SQLException {
        return getMessagesByScope("event_id", eventIds);
    }

    public Map<Integer, List<Message>> getGroupMessagesForGroups(List<Integer> groupIds) throws SQLException {
        return getMessagesByScope("group_id", groupIds);
    }

    public boolean canAccessGroupChat(int groupId, User loggedInUser) throws SQLException {
        if (loggedInUser == null) {
            return false;
        }
        if (loggedInUser.isAdmin()) {
            return true;
        }

        String sql = """
                SELECT g.id
                FROM user_groups g
                LEFT JOIN group_members gm ON g.id = gm.group_id
                WHERE g.id = ? AND (g.created_by = ? OR gm.user_id = ?)
                LIMIT 1
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, groupId);
            preparedStatement.setInt(2, loggedInUser.getId());
            preparedStatement.setInt(3, loggedInUser.getId());
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                return resultSet.next();
            }
        }
    }

    public boolean canAccessEventChat(int eventId, User loggedInUser) throws SQLException {
        return eventDao.canAccessEventInteraction(eventId, loggedInUser);
    }

    private List<Message> getMessages(String sql, int entityId) throws SQLException {
        List<Message> messages = new ArrayList<>();

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, entityId);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    Message message = new Message();
                    message.setId(resultSet.getInt("id"));
                    message.setSenderId(resultSet.getInt("sender_id"));
                    message.setSenderName(resultSet.getString("full_name"));
                    int groupId = resultSet.getInt("group_id");
                    message.setGroupId(resultSet.wasNull() ? null : groupId);
                    int eventId = resultSet.getInt("event_id");
                    message.setEventId(resultSet.wasNull() ? null : eventId);
                    message.setMessageText(resultSet.getString("message_text"));
                    message.setCreatedAt(resultSet.getTimestamp("created_at"));
                    messages.add(message);
                }
            }
        }

        return messages;
    }

    private Map<Integer, List<Message>> getMessagesByScope(String scopeColumn, List<Integer> ids) throws SQLException {
        Map<Integer, List<Message>> messagesByScope = new HashMap<>();
        if (ids == null || ids.isEmpty()) {
            return messagesByScope;
        }

        StringJoiner placeholders = new StringJoiner(", ");
        for (int ignored : ids) {
            placeholders.add("?");
        }

        String sql = """
                SELECT m.id, m.sender_id, m.group_id, m.event_id, m.message_text, m.created_at, u.full_name
                FROM messages m
                INNER JOIN users u ON m.sender_id = u.id
                WHERE %s IN (%s)
                ORDER BY m.created_at ASC, m.id ASC
                """.formatted(scopeColumn, placeholders);

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            for (int index = 0; index < ids.size(); index++) {
                preparedStatement.setInt(index + 1, ids.get(index));
            }

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    Message message = new Message();
                    message.setId(resultSet.getInt("id"));
                    message.setSenderId(resultSet.getInt("sender_id"));
                    message.setSenderName(resultSet.getString("full_name"));
                    int groupId = resultSet.getInt("group_id");
                    message.setGroupId(resultSet.wasNull() ? null : groupId);
                    int eventId = resultSet.getInt("event_id");
                    message.setEventId(resultSet.wasNull() ? null : eventId);
                    message.setMessageText(resultSet.getString("message_text"));
                    message.setCreatedAt(resultSet.getTimestamp("created_at"));

                    Integer scopeId = "event_id".equals(scopeColumn) ? message.getEventId() : message.getGroupId();
                    if (scopeId != null) {
                        messagesByScope.computeIfAbsent(scopeId, ignored -> new ArrayList<>()).add(message);
                    }
                }
            }
        }

        return messagesByScope;
    }
}
