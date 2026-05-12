package com.ems.dao;

import com.ems.model.DashboardStats;

import java.sql.SQLException;

public class DashboardDao {
    private final UserDao userDao = new UserDao();
    private final EventDao eventDao = new EventDao();
    private final GroupDao groupDao = new GroupDao();
    private final InvitationDao invitationDao = new InvitationDao();
    private final NotificationDao notificationDao = new NotificationDao();

    public DashboardStats getDashboardStats(int userId) throws SQLException {
        DashboardStats stats = new DashboardStats();
        stats.setTotalUsers(userDao.countUsers());
        stats.setTotalEvents(eventDao.countEvents());
        stats.setUpcomingEvents(eventDao.countUpcomingEvents());
        stats.setTotalGroups(groupDao.countGroups());
        stats.setPendingInvitations(invitationDao.countPendingInvitations(userId));
        stats.setUnreadNotifications(notificationDao.countUnreadNotifications(userId));
        return stats;
    }
}
