package com.ems.util;

import java.sql.SQLException;

public final class SqlErrorUtil {
    private SqlErrorUtil() {
    }

    public static boolean isMissingTable(SQLException exception, String tableName) {
        if (exception == null || tableName == null || tableName.isBlank()) {
            return false;
        }

        SQLException current = exception;
        while (current != null) {
            String sqlState = current.getSQLState();
            String message = current.getMessage();
            if ("42S02".equals(sqlState) || (message != null && message.toLowerCase().contains(tableName.toLowerCase()) && message.toLowerCase().contains("doesn't exist"))) {
                return true;
            }
            current = current.getNextException();
        }
        return false;
    }

    public static boolean isDuplicateEntry(SQLException exception) {
        SQLException current = exception;
        while (current != null) {
            String sqlState = current.getSQLState();
            if ("23000".equals(sqlState)) {
                return true;
            }
            current = current.getNextException();
        }
        return false;
    }
}
