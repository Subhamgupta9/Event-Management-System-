package com.ems.dao;

import com.ems.model.Group;
import com.ems.model.User;
import com.ems.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class GroupDao {
    private static final String CREATE_GROUPS_TABLE_SQL = """
            CREATE TABLE IF NOT EXISTS user_groups (
                id INT PRIMARY KEY AUTO_INCREMENT,
                name VARCHAR(100) NOT NULL UNIQUE,
                description VARCHAR(255) NOT NULL,
                created_by INT NOT NULL,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                CONSTRAINT fk_user_groups_users
                    FOREIGN KEY (created_by) REFERENCES users(id)
                    ON DELETE CASCADE
            )
            """;

    private static final String CREATE_GROUP_MEMBERS_TABLE_SQL = """
            CREATE TABLE IF NOT EXISTS group_members (
                group_id INT NOT NULL,
                user_id INT NOT NULL,
                added_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                PRIMARY KEY (group_id, user_id),
                CONSTRAINT fk_group_members_group
                    FOREIGN KEY (group_id) REFERENCES user_groups(id)
                    ON DELETE CASCADE,
                CONSTRAINT fk_group_members_user
                    FOREIGN KEY (user_id) REFERENCES users(id)
                    ON DELETE CASCADE
            )
            """;

    public boolean createGroup(Group group) throws SQLException {
        String sql = "INSERT INTO user_groups (name, description, created_by) VALUES (?, ?, ?)";

        try (Connection connection = DBConnection.getConnection()) {
            ensureGroupSchema(connection);

            try (PreparedStatement preparedStatement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                preparedStatement.setString(1, group.getName());
                preparedStatement.setString(2, group.getDescription());
                preparedStatement.setInt(3, group.getCreatedBy());
                boolean created = preparedStatement.executeUpdate() > 0;
                if (!created) {
                    return false;
                }

                try (ResultSet generatedKeys = preparedStatement.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        addMember(connection, generatedKeys.getInt(1), group.getCreatedBy());
                    }
                }
                return true;
            }
        }
    }

    public List<Group> getGroupsForUser(User loggedInUser) throws SQLException {
        List<Group> groups = new ArrayList<>();
        String sql = loggedInUser.isAdmin()
                ? """
                SELECT g.id, g.name, g.description, g.created_by, g.created_at, u.full_name
                FROM user_groups g
                INNER JOIN users u ON g.created_by = u.id
                ORDER BY g.id DESC
                """
                : """
                SELECT DISTINCT g.id, g.name, g.description, g.created_by, g.created_at, u.full_name
                FROM user_groups g
                INNER JOIN users u ON g.created_by = u.id
                LEFT JOIN group_members gm ON g.id = gm.group_id
                WHERE g.created_by = ? OR gm.user_id = ?
                ORDER BY g.id DESC
                """;

        try (Connection connection = DBConnection.getConnection()) {
            ensureGroupSchema(connection);

            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            if (!loggedInUser.isAdmin()) {
                preparedStatement.setInt(1, loggedInUser.getId());
                preparedStatement.setInt(2, loggedInUser.getId());
            }

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    Group group = mapGroup(resultSet);
                    group.setMembers(getMembersByGroupId(connection, group.getId()));
                    groups.add(group);
                }
            }
            }
        }

        return groups;
    }

    public List<Group> getManageableGroups(User loggedInUser) throws SQLException {
        List<Group> groups = new ArrayList<>();
        String sql = loggedInUser.isAdmin()
                ? "SELECT g.id, g.name, g.description, g.created_by, g.created_at, u.full_name FROM user_groups g INNER JOIN users u ON g.created_by = u.id ORDER BY g.name ASC"
                : "SELECT g.id, g.name, g.description, g.created_by, g.created_at, u.full_name FROM user_groups g INNER JOIN users u ON g.created_by = u.id WHERE g.created_by = ? ORDER BY g.name ASC";

        try (Connection connection = DBConnection.getConnection()) {
            ensureGroupSchema(connection);

            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            if (!loggedInUser.isAdmin()) {
                preparedStatement.setInt(1, loggedInUser.getId());
            }
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    groups.add(mapGroup(resultSet));
                }
            }
            }
        }

        return groups;
    }

    public boolean addMember(int groupId, int userId) throws SQLException {
        String sql = "INSERT IGNORE INTO group_members (group_id, user_id) VALUES (?, ?)";

        try (Connection connection = DBConnection.getConnection()) {
            ensureGroupSchema(connection);
            return addMember(connection, groupId, userId);
        }
    }

    public boolean removeMember(int groupId, int userId) throws SQLException {
        String sql = "DELETE FROM group_members WHERE group_id = ? AND user_id = ?";

        try (Connection connection = DBConnection.getConnection()) {
            ensureGroupSchema(connection);

            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, groupId);
            preparedStatement.setInt(2, userId);
            return preparedStatement.executeUpdate() > 0;
            }
        }
    }

    public boolean deleteGroup(int groupId) throws SQLException {
        String sql = "DELETE FROM user_groups WHERE id = ?";

        try (Connection connection = DBConnection.getConnection()) {
            ensureGroupSchema(connection);

            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                preparedStatement.setInt(1, groupId);
                return preparedStatement.executeUpdate() > 0;
            }
        }
    }

    public boolean canManageGroup(int groupId, User loggedInUser) throws SQLException {
        if (loggedInUser.isAdmin()) {
            return true;
        }

        try (Connection connection = DBConnection.getConnection()) {
            ensureGroupSchema(connection);

            try (PreparedStatement preparedStatement = connection.prepareStatement(
                     "SELECT id FROM user_groups WHERE id = ? AND created_by = ?")) {
            preparedStatement.setInt(1, groupId);
            preparedStatement.setInt(2, loggedInUser.getId());
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                return resultSet.next();
            }
            }
        }
    }

    public int countGroups() throws SQLException {
        try (Connection connection = DBConnection.getConnection()) {
            ensureGroupSchema(connection);

            try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT COUNT(*) FROM user_groups");
                 ResultSet resultSet = preparedStatement.executeQuery()) {
            resultSet.next();
            return resultSet.getInt(1);
            }
        }
    }

    public List<Integer> getMemberIdsByGroupId(int groupId) throws SQLException {
        List<Integer> userIds = new ArrayList<>();

        try (Connection connection = DBConnection.getConnection()) {
            ensureGroupSchema(connection);

            try (PreparedStatement preparedStatement = connection.prepareStatement(
                     "SELECT user_id FROM group_members WHERE group_id = ?")) {
            preparedStatement.setInt(1, groupId);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    userIds.add(resultSet.getInt("user_id"));
                }
            }
            }
        }

        return userIds;
    }

    private void ensureGroupSchema(Connection connection) throws SQLException {
        try (PreparedStatement createGroups = connection.prepareStatement(CREATE_GROUPS_TABLE_SQL)) {
            createGroups.executeUpdate();
        }

        try (PreparedStatement createMembers = connection.prepareStatement(CREATE_GROUP_MEMBERS_TABLE_SQL)) {
            createMembers.executeUpdate();
        }
    }

    private boolean addMember(Connection connection, int groupId, int userId) throws SQLException {
        String sql = "INSERT IGNORE INTO group_members (group_id, user_id) VALUES (?, ?)";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, groupId);
            preparedStatement.setInt(2, userId);
            return preparedStatement.executeUpdate() > 0;
        }
    }

    private Group mapGroup(ResultSet resultSet) throws SQLException {
        Group group = new Group();
        group.setId(resultSet.getInt("id"));
        group.setName(resultSet.getString("name"));
        group.setDescription(resultSet.getString("description"));
        group.setCreatedBy(resultSet.getInt("created_by"));
        group.setCreatedByName(resultSet.getString("full_name"));
        group.setCreatedAt(resultSet.getTimestamp("created_at"));
        return group;
    }

    private List<User> getMembersByGroupId(Connection connection, int groupId) throws SQLException {
        List<User> users = new ArrayList<>();
        String sql = """
                SELECT u.id, u.full_name, u.email, u.role, u.created_at
                FROM group_members gm
                INNER JOIN users u ON gm.user_id = u.id
                WHERE gm.group_id = ?
                ORDER BY u.full_name ASC
                """;

        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, groupId);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    User user = new User();
                    user.setId(resultSet.getInt("id"));
                    user.setFullName(resultSet.getString("full_name"));
                    user.setEmail(resultSet.getString("email"));
                    user.setRole(resultSet.getString("role"));
                    user.setCreatedAt(resultSet.getTimestamp("created_at"));
                    users.add(user);
                }
            }
        }

        return users;
    }
}
