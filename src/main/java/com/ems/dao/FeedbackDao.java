package com.ems.dao;

import com.ems.model.Feedback;
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

public class FeedbackDao {
    private static final String CREATE_FEEDBACK_TABLE_SQL = """
            CREATE TABLE IF NOT EXISTS feedback (
                id INT PRIMARY KEY AUTO_INCREMENT,
                event_id INT NOT NULL,
                user_id INT NOT NULL,
                rating TINYINT NOT NULL,
                comment VARCHAR(500) NULL,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                CONSTRAINT chk_feedback_rating CHECK (rating BETWEEN 1 AND 5),
                CONSTRAINT uq_feedback_event_user UNIQUE (event_id, user_id),
                CONSTRAINT fk_feedback_event
                    FOREIGN KEY (event_id) REFERENCES events(id)
                    ON DELETE CASCADE,
                CONSTRAINT fk_feedback_user
                    FOREIGN KEY (user_id) REFERENCES users(id)
                    ON DELETE CASCADE
            )
            """;

    private static final String CREATE_FEEDBACK_INDEX_SQL =
            "CREATE INDEX idx_feedback_event_created ON feedback(event_id, created_at)";

    public boolean saveOrUpdateFeedback(Feedback feedback) throws SQLException {
        if (feedback == null || feedback.getEventId() <= 0 || feedback.getUserId() <= 0) {
            throw new IllegalArgumentException("Valid feedback details are required.");
        }
        if (feedback.getRating() < 1 || feedback.getRating() > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5.");
        }

        String sql = """
                INSERT INTO feedback (event_id, user_id, rating, comment)
                VALUES (?, ?, ?, ?)
                ON DUPLICATE KEY UPDATE
                    rating = VALUES(rating),
                    comment = VALUES(comment),
                    created_at = CURRENT_TIMESTAMP
                """;

        try (Connection connection = DBConnection.getConnection()) {
            ensureFeedbackSchema(connection);

            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                preparedStatement.setInt(1, feedback.getEventId());
                preparedStatement.setInt(2, feedback.getUserId());
                preparedStatement.setInt(3, feedback.getRating());
                preparedStatement.setString(4, feedback.getComment());
                return preparedStatement.executeUpdate() > 0;
            }
        }
    }

    public List<Feedback> getFeedbackForEvent(int eventId) throws SQLException {
        List<Feedback> feedbackList = new ArrayList<>();
        String sql = """
                SELECT f.id, f.event_id, f.user_id, f.rating, f.comment, f.created_at, u.full_name
                FROM feedback f
                INNER JOIN users u ON f.user_id = u.id
                WHERE f.event_id = ?
                ORDER BY f.created_at DESC, f.id DESC
                """;

        try (Connection connection = DBConnection.getConnection()) {
            ensureFeedbackSchema(connection);

            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                preparedStatement.setInt(1, eventId);

                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    while (resultSet.next()) {
                        Feedback feedback = mapFeedback(resultSet);
                        feedbackList.add(feedback);
                    }
                }
            }
        }

        return feedbackList;
    }

    public Map<Integer, List<Feedback>> getFeedbackForEvents(List<Integer> eventIds) throws SQLException {
        Map<Integer, List<Feedback>> feedbackByEvent = new HashMap<>();
        if (eventIds == null || eventIds.isEmpty()) {
            return feedbackByEvent;
        }

        StringJoiner placeholders = new StringJoiner(", ");
        for (int ignored : eventIds) {
            placeholders.add("?");
        }

        String sql = """
                SELECT f.id, f.event_id, f.user_id, f.rating, f.comment, f.created_at, u.full_name
                FROM feedback f
                INNER JOIN users u ON f.user_id = u.id
                WHERE f.event_id IN (%s)
                ORDER BY f.created_at DESC, f.id DESC
                """.formatted(placeholders);

        try (Connection connection = DBConnection.getConnection()) {
            ensureFeedbackSchema(connection);

            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                for (int index = 0; index < eventIds.size(); index++) {
                    preparedStatement.setInt(index + 1, eventIds.get(index));
                }

                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    while (resultSet.next()) {
                        Feedback feedback = mapFeedback(resultSet);
                        feedbackByEvent.computeIfAbsent(feedback.getEventId(), ignored -> new ArrayList<>()).add(feedback);
                    }
                }
            }
        }

        return feedbackByEvent;
    }

    private void ensureFeedbackSchema(Connection connection) throws SQLException {
        try (PreparedStatement createTable = connection.prepareStatement(CREATE_FEEDBACK_TABLE_SQL)) {
            createTable.executeUpdate();
        }

        if (!indexExists(connection, "feedback", "idx_feedback_event_created")) {
            try (PreparedStatement createIndex = connection.prepareStatement(CREATE_FEEDBACK_INDEX_SQL)) {
                createIndex.executeUpdate();
            }
        }
    }

    private boolean indexExists(Connection connection, String tableName, String indexName) throws SQLException {
        try (ResultSet resultSet = connection.getMetaData().getIndexInfo(connection.getCatalog(), null, tableName, false, false)) {
            while (resultSet.next()) {
                String existingIndexName = resultSet.getString("INDEX_NAME");
                if (indexName.equalsIgnoreCase(existingIndexName)) {
                    return true;
                }
            }
        }
        return false;
    }

    private Feedback mapFeedback(ResultSet resultSet) throws SQLException {
        Feedback feedback = new Feedback();
        feedback.setId(resultSet.getInt("id"));
        feedback.setEventId(resultSet.getInt("event_id"));
        feedback.setUserId(resultSet.getInt("user_id"));
        feedback.setUserName(resultSet.getString("full_name"));
        feedback.setRating(resultSet.getInt("rating"));
        feedback.setComment(resultSet.getString("comment"));
        feedback.setCreatedAt(resultSet.getTimestamp("created_at"));
        return feedback;
    }
}
