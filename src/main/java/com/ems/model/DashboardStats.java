package com.ems.model;

public class DashboardStats {
    private int totalUsers;
    private int totalEvents;
    private int upcomingEvents;
    private int totalGroups;
    private int pendingInvitations;
    private int unreadNotifications;

    public int getTotalUsers() {
        return totalUsers;
    }

    public void setTotalUsers(int totalUsers) {
        this.totalUsers = totalUsers;
    }

    public int getTotalEvents() {
        return totalEvents;
    }

    public void setTotalEvents(int totalEvents) {
        this.totalEvents = totalEvents;
    }

    public int getUpcomingEvents() {
        return upcomingEvents;
    }

    public void setUpcomingEvents(int upcomingEvents) {
        this.upcomingEvents = upcomingEvents;
    }

    public int getTotalGroups() {
        return totalGroups;
    }

    public void setTotalGroups(int totalGroups) {
        this.totalGroups = totalGroups;
    }

    public int getPendingInvitations() {
        return pendingInvitations;
    }

    public void setPendingInvitations(int pendingInvitations) {
        this.pendingInvitations = pendingInvitations;
    }

    public int getUnreadNotifications() {
        return unreadNotifications;
    }

    public void setUnreadNotifications(int unreadNotifications) {
        this.unreadNotifications = unreadNotifications;
    }
}
