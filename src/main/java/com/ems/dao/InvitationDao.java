package com.ems.dao;

import com.ems.model.Invitation;
import com.ems.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class InvitationDao {
    public boolean sendInvitationToUser(int eventId, int invitedUserId, Integer sourceGroupId, int sentBy, String message)
            throws SQLException {
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement updateStatement = connection.prepareStatement("""
                     UPDATE invitations
                     SET sent_by = ?, message = ?, status = 'PENDING', created_at = CURRENT_TIMESTAMP, responded_at = NULL
                     WHERE event_id = ? AND invited_user_id = ?
                       AND ((source_group_id IS NULL AND ? IS NULL) OR source_group_id = ?)
                     """);
             PreparedStatement insertStatement = connection.prepareStatement("""
                     INSERT INTO invitations (event_id, invited_user_id, source_group_id, sent_by, message, status)
                     VALUES (?, ?, ?, ?, ?, 'PENDING')
                     """)) {
            updateStatement.setInt(1, sentBy);
            updateStatement.setString(2, message);
            updateStatement.setInt(3, eventId);
            updateStatement.setInt(4, invitedUserId);
            if (sourceGroupId == null) {
                updateStatement.setNull(5, java.sql.Types.INTEGER);
                updateStatement.setNull(6, java.sql.Types.INTEGER);
            } else {
                updateStatement.setInt(5, sourceGroupId);
                updateStatement.setInt(6, sourceGroupId);
            }

            if (updateStatement.executeUpdate() > 0) {
                return true;
            }

            insertStatement.setInt(1, eventId);
            insertStatement.setInt(2, invitedUserId);
            if (sourceGroupId == null) {
                insertStatement.setNull(3, java.sql.Types.INTEGER);
            } else {
                insertStatement.setInt(3, sourceGroupId);
            }
            insertStatement.setInt(4, sentBy);
            insertStatement.setString(5, message);
            return insertStatement.executeUpdate() > 0;
        }
    }

    public List<Invitation> getInvitationsForUser(int userId) throws SQLException {
        List<Invitation> invitations = new ArrayList<>();
        String sql = """
                SELECT i.id, i.event_id, e.title AS event_title, i.invited_user_id, iu.full_name AS invited_user_name,
                       i.source_group_id, g.name AS source_group_name, i.sent_by, su.full_name AS sent_by_name,
                       i.message, i.status, i.created_at, i.responded_at
                FROM invitations i
                INNER JOIN events e ON i.event_id = e.id
                INNER JOIN users iu ON i.invited_user_id = iu.id
                INNER JOIN users su ON i.sent_by = su.id
                LEFT JOIN user_groups g ON i.source_group_id = g.id
                WHERE i.invited_user_id = ?
                ORDER BY i.created_at DESC
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, userId);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    invitations.add(mapInvitation(resultSet));
                }
            }
        }

        return invitations;
    }

    public boolean updateInvitationStatus(int invitationId, int userId, String status) throws SQLException {
        String sql = "UPDATE invitations SET status = ?, responded_at = CURRENT_TIMESTAMP WHERE id = ? AND invited_user_id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, status);
            preparedStatement.setInt(2, invitationId);
            preparedStatement.setInt(3, userId);
            return preparedStatement.executeUpdate() > 0;
        }
    }

    public Invitation getInvitationById(int invitationId) throws SQLException {
        String sql = """
                SELECT i.id, i.event_id, e.title AS event_title, i.invited_user_id, iu.full_name AS invited_user_name,
                       i.source_group_id, g.name AS source_group_name, i.sent_by, su.full_name AS sent_by_name,
                       i.message, i.status, i.created_at, i.responded_at
                FROM invitations i
                INNER JOIN events e ON i.event_id = e.id
                INNER JOIN users iu ON i.invited_user_id = iu.id
                INNER JOIN users su ON i.sent_by = su.id
                LEFT JOIN user_groups g ON i.source_group_id = g.id
                WHERE i.id = ?
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, invitationId);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return mapInvitation(resultSet);
                }
            }
        }

        return null;
    }

    public int countPendingInvitations(int userId) throws SQLException {
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(
                     "SELECT COUNT(*) FROM invitations WHERE invited_user_id = ? AND status = 'PENDING'")) {
            preparedStatement.setInt(1, userId);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                resultSet.next();
                return resultSet.getInt(1);
            }
        }
    }

    private Invitation mapInvitation(ResultSet resultSet) throws SQLException {
        Invitation invitation = new Invitation();
        invitation.setId(resultSet.getInt("id"));
        invitation.setEventId(resultSet.getInt("event_id"));
        invitation.setEventTitle(resultSet.getString("event_title"));
        invitation.setInvitedUserId(resultSet.getInt("invited_user_id"));
        invitation.setInvitedUserName(resultSet.getString("invited_user_name"));
        Object sourceGroupId = resultSet.getObject("source_group_id");
        invitation.setSourceGroupId(sourceGroupId == null ? null : resultSet.getInt("source_group_id"));
        invitation.setSourceGroupName(resultSet.getString("source_group_name"));
        invitation.setSentBy(resultSet.getInt("sent_by"));
        invitation.setSentByName(resultSet.getString("sent_by_name"));
        invitation.setMessage(resultSet.getString("message"));
        invitation.setStatus(resultSet.getString("status"));
        invitation.setCreatedAt(resultSet.getTimestamp("created_at"));
        invitation.setRespondedAt(resultSet.getTimestamp("responded_at"));
        return invitation;
    }
}
