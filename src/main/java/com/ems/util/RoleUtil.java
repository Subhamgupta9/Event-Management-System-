package com.ems.util;

import com.ems.model.User;

public final class RoleUtil {
    private RoleUtil() {
    }

    public static boolean canManageEvents(User user) {
        return user != null && (user.isAdmin() || user.isOrganizer());
    }

    public static boolean canManageGroups(User user) {
        return user != null && (user.isAdmin() || user.isOrganizer());
    }

    public static boolean canSendInvitations(User user) {
        return user != null && (user.isAdmin() || user.isOrganizer());
    }

    public static boolean isAdmin(User user) {
        return user != null && user.isAdmin();
    }
}
