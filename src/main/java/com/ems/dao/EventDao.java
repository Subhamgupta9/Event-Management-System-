package com.ems.dao;

import com.ems.model.Event;
import com.ems.model.User;
import com.ems.util.DBConnection;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

public class EventDao {
    public boolean createEvent(Event event) throws SQLException {
        String sql = "INSERT INTO events (title, description, event_date, location, created_by) VALUES (?, ?, ?, ?, ?)";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            preparedStatement.setString(1, event.getTitle());
            preparedStatement.setString(2, event.getDescription());
            preparedStatement.setDate(3, Date.valueOf(event.getEventDate()));
            preparedStatement.setString(4, event.getLocation());
            preparedStatement.setInt(5, event.getCreatedBy());
            boolean isCreated = preparedStatement.executeUpdate() > 0;

            if (isCreated) {
                try (ResultSet generatedKeys = preparedStatement.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        syncEventGroups(connection, generatedKeys.getInt(1), event.getAssignedGroupIds());
                    }
                }
            }

            return isCreated;
        }
    }

    public List<Event> getAllEvents() throws SQLException {
        return searchEvents(null, null, null, null, null);
    }

    public List<Event> searchEvents(String keyword, String location, String fromDate, String toDate, Integer groupId)
            throws SQLException {
        return searchEventsInternal(keyword, location, fromDate, toDate, groupId, null);
    }

    public List<Event> searchEventsForUser(User loggedInUser, String keyword, String location, String fromDate, String toDate, Integer groupId)
            throws SQLException {
        if (loggedInUser == null) {
            return List.of();
        }
        if (loggedInUser.isAdmin()) {
            return searchEvents(keyword, location, fromDate, toDate, groupId);
        }
        return searchEventsInternal(keyword, location, fromDate, toDate, groupId, loggedInUser);
    }

    public Event getEventById(int eventId) throws SQLException {
        String sql = """
                SELECT e.id, e.title, e.description, e.event_date, e.location, e.created_by, e.created_at, u.full_name,
                       GROUP_CONCAT(DISTINCT g.name ORDER BY g.name SEPARATOR ', ') AS group_names
                FROM events e
                INNER JOIN users u ON e.created_by = u.id
                LEFT JOIN event_groups eg ON e.id = eg.event_id
                LEFT JOIN user_groups g ON eg.group_id = g.id
                WHERE e.id = ?
                GROUP BY e.id, e.title, e.description, e.event_date, e.location, e.created_by, e.created_at, u.full_name
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, eventId);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    Event event = mapEvent(resultSet);
                    event.setAssignedGroupIds(getAssignedGroupIds(connection, eventId));
                    return event;
                }
            }
        }

        return null;
    }

    public boolean canManageEvent(int eventId, User loggedInUser) throws SQLException {
        if (loggedInUser == null) {
            return false;
        }

        if (loggedInUser.isAdmin()) {
            return true;
        }

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(
                     "SELECT id FROM events WHERE id = ? AND created_by = ?")) {
            preparedStatement.setInt(1, eventId);
            preparedStatement.setInt(2, loggedInUser.getId());
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                return resultSet.next();
            }
        }
    }

    public boolean updateEvent(Event event, User loggedInUser) throws SQLException {
        String sql = loggedInUser.isAdmin()
                ? "UPDATE events SET title = ?, description = ?, event_date = ?, location = ? WHERE id = ?"
                : "UPDATE events SET title = ?, description = ?, event_date = ?, location = ? WHERE id = ? AND created_by = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, event.getTitle());
            preparedStatement.setString(2, event.getDescription());
            preparedStatement.setDate(3, Date.valueOf(event.getEventDate()));
            preparedStatement.setString(4, event.getLocation());
            preparedStatement.setInt(5, event.getId());
            if (!loggedInUser.isAdmin()) {
                preparedStatement.setInt(6, event.getCreatedBy());
            }

            boolean isUpdated = preparedStatement.executeUpdate() > 0;
            if (isUpdated) {
                syncEventGroups(connection, event.getId(), event.getAssignedGroupIds());
            }
            return isUpdated;
        }
    }

    public boolean deleteEvent(int eventId, User loggedInUser) throws SQLException {
        String sql = loggedInUser.isAdmin()
                ? "DELETE FROM events WHERE id = ?"
                : "DELETE FROM events WHERE id = ? AND created_by = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, eventId);
            if (!loggedInUser.isAdmin()) {
                preparedStatement.setInt(2, loggedInUser.getId());
            }
            return preparedStatement.executeUpdate() > 0;
        }
    }

    public List<Event> getEventsForCreator(User user) throws SQLException {
        String sql = user.isAdmin()
                ? """
                SELECT e.id, e.title, e.description, e.event_date, e.location, e.created_by, e.created_at, u.full_name,
                       GROUP_CONCAT(DISTINCT g.name ORDER BY g.name SEPARATOR ', ') AS group_names
                FROM events e
                INNER JOIN users u ON e.created_by = u.id
                LEFT JOIN event_groups eg ON e.id = eg.event_id
                LEFT JOIN user_groups g ON eg.group_id = g.id
                GROUP BY e.id, e.title, e.description, e.event_date, e.location, e.created_by, e.created_at, u.full_name
                ORDER BY e.event_date ASC, e.id DESC
                """
                : """
                SELECT e.id, e.title, e.description, e.event_date, e.location, e.created_by, e.created_at, u.full_name,
                       GROUP_CONCAT(DISTINCT g.name ORDER BY g.name SEPARATOR ', ') AS group_names
                FROM events e
                INNER JOIN users u ON e.created_by = u.id
                LEFT JOIN event_groups eg ON e.id = eg.event_id
                LEFT JOIN user_groups g ON eg.group_id = g.id
                WHERE e.created_by = ?
                GROUP BY e.id, e.title, e.description, e.event_date, e.location, e.created_by, e.created_at, u.full_name
                ORDER BY e.event_date ASC, e.id DESC
                """;

        List<Event> events = new ArrayList<>();
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            if (!user.isAdmin()) {
                preparedStatement.setInt(1, user.getId());
            }
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    events.add(mapEvent(resultSet));
                }
            }
        }
        return events;
    }

    public int countEvents() throws SQLException {
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement("SELECT COUNT(*) FROM events");
             ResultSet resultSet = preparedStatement.executeQuery()) {
            resultSet.next();
            return resultSet.getInt(1);
        }
    }

    public int countUpcomingEvents() throws SQLException {
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement("SELECT COUNT(*) FROM events WHERE event_date >= CURDATE()");
             ResultSet resultSet = preparedStatement.executeQuery()) {
            resultSet.next();
            return resultSet.getInt(1);
        }
    }

    public boolean eventExists(int eventId) throws SQLException {
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement("SELECT id FROM events WHERE id = ?")) {
            preparedStatement.setInt(1, eventId);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                return resultSet.next();
            }
        }
    }

    public boolean canAccessEventInteraction(int eventId, User loggedInUser) throws SQLException {
        if (loggedInUser == null) {
            return false;
        }
        if (loggedInUser.isAdmin()) {
            return true;
        }

        String sql = """
                SELECT DISTINCT e.id
                FROM events e
                LEFT JOIN invitations i
                    ON i.event_id = e.id
                   AND i.invited_user_id = ?
                   AND i.status = 'ACCEPTED'
                LEFT JOIN event_groups eg
                    ON eg.event_id = e.id
                LEFT JOIN user_groups g
                    ON g.id = eg.group_id
                LEFT JOIN group_members gm
                    ON gm.group_id = eg.group_id
                   AND gm.user_id = ?
                WHERE e.id = ?
                  AND (
                      e.created_by = ?
                      OR i.id IS NOT NULL
                      OR gm.user_id IS NOT NULL
                      OR g.created_by = ?
                  )
                LIMIT 1
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, loggedInUser.getId());
            preparedStatement.setInt(2, loggedInUser.getId());
            preparedStatement.setInt(3, eventId);
            preparedStatement.setInt(4, loggedInUser.getId());
            preparedStatement.setInt(5, loggedInUser.getId());
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                return resultSet.next();
            }
        }
    }

    public Map<Integer, Integer> getParticipantCounts(List<Integer> eventIds) throws SQLException {
        Map<Integer, Integer> participantCounts = new HashMap<>();
        if (eventIds == null || eventIds.isEmpty()) {
            return participantCounts;
        }

        StringJoiner placeholders = new StringJoiner(", ");
        for (int ignored : eventIds) {
            placeholders.add("?");
        }

        String sql = """
                SELECT participant_data.event_id, COUNT(DISTINCT participant_data.user_id) AS participant_count
                FROM (
                    SELECT e.id AS event_id, e.created_by AS user_id
                    FROM events e
                    WHERE e.id IN (%1$s)

                    UNION ALL

                    SELECT i.event_id, i.invited_user_id
                    FROM invitations i
                    WHERE i.status = 'ACCEPTED'
                      AND i.event_id IN (%1$s)

                    UNION ALL

                    SELECT eg.event_id, gm.user_id
                    FROM event_groups eg
                    INNER JOIN group_members gm ON gm.group_id = eg.group_id
                    WHERE eg.event_id IN (%1$s)

                    UNION ALL

                    SELECT eg.event_id, g.created_by
                    FROM event_groups eg
                    INNER JOIN user_groups g ON g.id = eg.group_id
                    WHERE eg.event_id IN (%1$s)
                ) participant_data
                GROUP BY participant_data.event_id
                """.formatted(placeholders);

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            int parameterIndex = 1;
            for (int repeat = 0; repeat < 4; repeat++) {
                for (Integer eventId : eventIds) {
                    preparedStatement.setInt(parameterIndex++, eventId);
                }
            }

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    participantCounts.put(resultSet.getInt("event_id"), resultSet.getInt("participant_count"));
                }
            }
        }

        return participantCounts;
    }

    private List<Event> searchEventsInternal(String keyword, String location, String fromDate, String toDate, Integer groupId, User visibilityUser)
            throws SQLException {
        List<Event> events = new ArrayList<>();
        StringBuilder sql = new StringBuilder("""
                SELECT e.id, e.title, e.description, e.event_date, e.location, e.created_by, e.created_at, u.full_name,
                       GROUP_CONCAT(DISTINCT g.name ORDER BY g.name SEPARATOR ', ') AS group_names
                FROM events e
                INNER JOIN users u ON e.created_by = u.id
                LEFT JOIN event_groups eg ON e.id = eg.event_id
                LEFT JOIN user_groups g ON eg.group_id = g.id
                """);
        List<Object> parameters = new ArrayList<>();

        if (visibilityUser != null) {
            sql.append("""
                    LEFT JOIN invitations i
                        ON i.event_id = e.id
                       AND i.invited_user_id = ?
                       AND i.status = 'ACCEPTED'
                    LEFT JOIN group_members gm
                        ON gm.group_id = eg.group_id
                       AND gm.user_id = ?
                    """);
            parameters.add(visibilityUser.getId());
            parameters.add(visibilityUser.getId());
        }

        sql.append(" WHERE 1 = 1");

        if (visibilityUser != null) {
            sql.append("""
                     AND (
                         e.created_by = ?
                         OR i.id IS NOT NULL
                         OR gm.user_id IS NOT NULL
                         OR g.created_by = ?
                     )
                    """);
            parameters.add(visibilityUser.getId());
            parameters.add(visibilityUser.getId());
        }

        if (keyword != null && !keyword.isBlank()) {
            sql.append(" AND (e.title LIKE ? OR e.description LIKE ?)");
            String pattern = "%" + keyword.trim() + "%";
            parameters.add(pattern);
            parameters.add(pattern);
        }
        if (location != null && !location.isBlank()) {
            sql.append(" AND e.location LIKE ?");
            parameters.add("%" + location.trim() + "%");
        }
        if (fromDate != null && !fromDate.isBlank()) {
            sql.append(" AND e.event_date >= ?");
            parameters.add(Date.valueOf(fromDate));
        }
        if (toDate != null && !toDate.isBlank()) {
            sql.append(" AND e.event_date <= ?");
            parameters.add(Date.valueOf(toDate));
        }
        if (groupId != null && groupId > 0) {
            sql.append(" AND eg.group_id = ?");
            parameters.add(groupId);
        }

        sql.append("""
                 GROUP BY e.id, e.title, e.description, e.event_date, e.location, e.created_by, e.created_at, u.full_name
                 ORDER BY e.event_date ASC, e.id DESC
                """);

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql.toString())) {
            setParameters(preparedStatement, parameters);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    events.add(mapEvent(resultSet));
                }
            }
        }

        return events;
    }

    private Event mapEvent(ResultSet resultSet) throws SQLException {
        Event event = new Event();
        event.setId(resultSet.getInt("id"));
        event.setTitle(resultSet.getString("title"));
        event.setDescription(resultSet.getString("description"));
        event.setEventDate(resultSet.getDate("event_date").toLocalDate());
        event.setLocation(resultSet.getString("location"));
        event.setCreatedBy(resultSet.getInt("created_by"));
        event.setCreatedByName(resultSet.getString("full_name"));
        event.setAssignedGroupNames(resultSet.getString("group_names"));
        event.setCreatedAt(resultSet.getTimestamp("created_at"));
        return event;
    }

    private void syncEventGroups(Connection connection, int eventId, List<Integer> groupIds) throws SQLException {
        try (PreparedStatement deleteStatement = connection.prepareStatement("DELETE FROM event_groups WHERE event_id = ?")) {
            deleteStatement.setInt(1, eventId);
            deleteStatement.executeUpdate();
        }

        if (groupIds == null || groupIds.isEmpty()) {
            return;
        }

        try (PreparedStatement insertStatement = connection.prepareStatement(
                "INSERT INTO event_groups (event_id, group_id) VALUES (?, ?)")) {
            for (Integer groupId : groupIds) {
                insertStatement.setInt(1, eventId);
                insertStatement.setInt(2, groupId);
                insertStatement.addBatch();
            }
            insertStatement.executeBatch();
        }
    }

    private List<Integer> getAssignedGroupIds(Connection connection, int eventId) throws SQLException {
        List<Integer> groupIds = new ArrayList<>();
        try (PreparedStatement preparedStatement = connection.prepareStatement(
                "SELECT group_id FROM event_groups WHERE event_id = ? ORDER BY group_id")) {
            preparedStatement.setInt(1, eventId);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    groupIds.add(resultSet.getInt("group_id"));
                }
            }
        }
        return groupIds;
    }

    private void setParameters(PreparedStatement preparedStatement, List<Object> parameters) throws SQLException {
        for (int index = 0; index < parameters.size(); index++) {
            Object parameter = parameters.get(index);
            if (parameter instanceof Date date) {
                preparedStatement.setDate(index + 1, date);
            } else {
                preparedStatement.setObject(index + 1, parameter);
            }
        }
    }
}
