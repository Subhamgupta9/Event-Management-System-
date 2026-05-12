<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<%@ page import="com.ems.model.Event" %>
<%@ page import="com.ems.model.Feedback" %>
<%@ page import="com.ems.model.Group" %>
<%@ page import="com.ems.model.Invitation" %>
<%@ page import="com.ems.model.Notification" %>
<%@ page import="com.ems.model.User" %>
<%@ page import="com.ems.model.DashboardStats" %>
<%@ page import="com.ems.util.HtmlUtil" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%! 
    private String h(String value) { return HtmlUtil.escape(value); }
    private String safeUrl(String value) { return HtmlUtil.safeUrl(value); }
%>
<%
    User loggedInUser = (User) session.getAttribute("loggedInUser");
    List<Event> eventList = (List<Event>) request.getAttribute("eventList");
    List<Event> manageableEventList = (List<Event>) request.getAttribute("manageableEventList");
    List<Group> manageableGroups = (List<Group>) request.getAttribute("manageableGroups");
    List<Group> visibleGroups = (List<Group>) request.getAttribute("visibleGroups");
    List<User> assignableUsers = (List<User>) request.getAttribute("assignableUsers");
    List<Invitation> invitationList = (List<Invitation>) request.getAttribute("invitationList");
    List<Notification> notificationList = (List<Notification>) request.getAttribute("notificationList");
    DashboardStats stats = (DashboardStats) request.getAttribute("stats");
    Map<Integer, List<Feedback>> eventFeedback = (Map<Integer, List<Feedback>>) request.getAttribute("eventFeedback");
    Map<Integer, Feedback> currentUserFeedback = (Map<Integer, Feedback>) request.getAttribute("currentUserFeedback");
    Map<Integer, Boolean> eventInteractionAccess = (Map<Integer, Boolean>) request.getAttribute("eventInteractionAccess");
    Map<Integer, Integer> participantCountByEvent = (Map<Integer, Integer>) request.getAttribute("participantCountByEvent");
    Boolean canManageEvents = (Boolean) request.getAttribute("canManageEvents");
    Boolean canManageGroups = (Boolean) request.getAttribute("canManageGroups");
    Boolean canSendInvitations = (Boolean) request.getAttribute("canSendInvitations");
    String success = request.getParameter("success");
    String error = request.getParameter("error");
    String validationError = (String) request.getAttribute("validationError");
    String keyword = (String) request.getAttribute("keyword");
    String location = (String) request.getAttribute("location");
    String fromDate = (String) request.getAttribute("fromDate");
    String toDate = (String) request.getAttribute("toDate");
    Integer selectedGroupId = (Integer) request.getAttribute("groupId");
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Dashboard - Event Management System</title>
    <link rel="stylesheet" href="<%= request.getContextPath() %>/css/style.css">
</head>
<body class="dashboard-body">
<div class="page-container">
    <header class="topbar">
        <div>
            <h1>Dashboard</h1>
            <p>Welcome, <strong><%= h(loggedInUser.getFullName()) %></strong> (<%= h(loggedInUser.getRole()) %>)</p>
        </div>
        <div class="topbar-actions">
            <% if (Boolean.TRUE.equals(canManageEvents)) { %>
            <a class="btn secondary" href="<%= request.getContextPath() %>/events/create">Create Event</a>
            <% } %>
            <a class="btn secondary" href="<%= request.getContextPath() %>/groups">Open Groups</a>
            <% if (Boolean.TRUE.equals(canManageGroups)) { %>
            <a class="btn secondary" href="<%= request.getContextPath() %>/groups">Manage Groups</a>
            <% } %>
            <% if (loggedInUser.isAdmin()) { %>
            <a class="btn secondary" href="<%= request.getContextPath() %>/admin/panel">Admin Panel</a>
            <% } %>
            <a class="btn danger-outline" href="<%= request.getContextPath() %>/logout">Logout</a>
        </div>
    </header>

    <% if ("created".equals(success)) { %>
    <div class="message success">Event created successfully.</div>
    <% } else if ("updated".equals(success)) { %>
    <div class="message success">Event updated successfully.</div>
    <% } else if ("deleted".equals(success)) { %>
    <div class="message success">Event deleted successfully.</div>
    <% } else if ("inviteSent".equals(success)) { %>
    <div class="message success">Invitation sent successfully.</div>
    <% } else if ("inviteUpdated".equals(success)) { %>
    <div class="message success">Invitation response saved.</div>
    <% } else if ("notificationsRead".equals(success)) { %>
    <div class="message success">Notifications marked as read.</div>
    <% } else if ("notificationsCleared".equals(success)) { %>
    <div class="message success">All notifications cleared.</div>
    <% } else if ("feedbackSaved".equals(success)) { %>
    <div class="message success">Feedback saved successfully.</div>
    <% } else if ("notfound".equals(success)) { %>
    <div class="message error">Requested record was not found.</div>
    <% } %>

    <% if ("unauthorized".equals(error)) { %>
    <div class="message error">You do not have permission for that action.</div>
    <% } else if ("invalidAction".equals(error)) { %>
    <div class="message error">Please choose a valid dashboard action.</div>
    <% } else if ("feedbackTooLong".equals(error)) { %>
    <div class="message error">Feedback comment cannot exceed 500 characters.</div>
    <% } else if ("feedbackUnavailable".equals(error)) { %>
    <div class="message error">Feedback is currently unavailable. Create or repair the feedback table, then try again.</div>
    <% } %>

    <% if (validationError != null) { %>
    <div class="message error"><%= h(validationError) %></div>
    <% } %>

    <section class="stats-grid">
        <div class="stat-card">
            <span class="stat-label">Total Users</span>
            <strong><%= stats != null ? stats.getTotalUsers() : 0 %></strong>
        </div>
        <div class="stat-card">
            <span class="stat-label">Total Events</span>
            <strong><%= stats != null ? stats.getTotalEvents() : 0 %></strong>
        </div>
        <div class="stat-card">
            <span class="stat-label">Upcoming Events</span>
            <strong><%= stats != null ? stats.getUpcomingEvents() : 0 %></strong>
        </div>
        <div class="stat-card">
            <span class="stat-label">Unread Notifications</span>
            <strong><%= stats != null ? stats.getUnreadNotifications() : 0 %></strong>
        </div>
        <div class="stat-card">
            <span class="stat-label">Groups</span>
            <strong><%= stats != null ? stats.getTotalGroups() : 0 %></strong>
        </div>
        <div class="stat-card">
            <span class="stat-label">Pending Invitations</span>
            <strong><%= stats != null ? stats.getPendingInvitations() : 0 %></strong>
        </div>
    </section>

    <section class="panel-grid">
        <div class="table-card">
            <div class="section-header">
                <h2>Search & Filter Events</h2>
            </div>
            <form action="<%= request.getContextPath() %>/dashboard" method="get" class="grid-form">
                <div>
                    <label for="keyword">Keyword</label>
                    <input type="text" id="keyword" name="keyword" value="<%= h(keyword != null ? keyword : "") %>" placeholder="Search title or description">
                </div>
                <div>
                    <label for="location">Location</label>
                    <input type="text" id="location" name="location" value="<%= h(location != null ? location : "") %>" placeholder="Search location">
                </div>
                <div>
                    <label for="fromDate">From Date</label>
                    <input type="date" id="fromDate" name="fromDate" value="<%= fromDate != null ? fromDate : "" %>">
                </div>
                <div>
                    <label for="toDate">To Date</label>
                    <input type="date" id="toDate" name="toDate" value="<%= toDate != null ? toDate : "" %>">
                </div>
                <div>
                    <label for="groupId">Group</label>
                    <select id="groupId" name="groupId">
                        <option value="">All Groups</option>
                        <%
                            if (visibleGroups != null) {
                                for (Group group : visibleGroups) {
                        %>
                        <option value="<%= group.getId() %>" <%= selectedGroupId != null && selectedGroupId == group.getId() ? "selected" : "" %>><%= h(group.getName()) %></option>
                        <%
                                }
                            }
                        %>
                    </select>
                </div>
                <div class="search-actions">
                    <button type="submit">Apply Filters</button>
                    <a class="btn secondary" href="<%= request.getContextPath() %>/dashboard">Reset</a>
                </div>
            </form>
        </div>

        <div class="table-card">
            <div class="section-header">
                <h2>Notifications</h2>
                <div class="action-row">
                    <form action="<%= request.getContextPath() %>/notifications/read" method="post" class="inline-form">
                        <button type="submit" class="btn secondary compact-button">Mark All Read</button>
                    </form>
                    <form action="<%= request.getContextPath() %>/notifications/read" method="post" class="inline-form">
                        <input type="hidden" name="action" value="clearAll">
                        <button type="submit" class="btn danger-outline compact-button" onclick="return confirm('Clear all notifications?');">Clear All</button>
                    </form>
                </div>
            </div>
            <div class="list-panel">
                <%
                    if (notificationList == null || notificationList.isEmpty()) {
                %>
                <p class="muted">No notifications available.</p>
                <% } else {
                    for (Notification notification : notificationList) {
                %>
                <div class="list-item <%= notification.isRead() ? "" : "highlight-item" %>">
                    <p><%= h(notification.getMessage()) %></p>
                    <% if (notification.getLinkUrl() != null && !notification.getLinkUrl().isBlank()) { %>
                    <a class="inline-link" href="<%= safeUrl(notification.getLinkUrl()) %>">Open</a>
                    <% } %>
                    <span class="muted small-text"><%= notification.getCreatedAt() %></span>
                </div>
                <%
                    }
                }
                %>
            </div>
        </div>
    </section>

    <section class="panel-grid">
        <div class="table-card">
            <div class="section-header">
                <h2>Invitations</h2>
                <span class="badge"><%= invitationList == null ? 0 : invitationList.size() %> total</span>
            </div>
            <div class="list-panel">
                <%
                    if (invitationList == null || invitationList.isEmpty()) {
                %>
                <p class="muted">No invitations received yet.</p>
                <% } else {
                    for (Invitation invitation : invitationList) {
                %>
                <div class="list-item">
                    <p><strong><%= h(invitation.getEventTitle()) %></strong> by <%= h(invitation.getSentByName()) %></p>
                    <p class="muted"><%= h(invitation.getMessage() != null ? invitation.getMessage() : "No message provided.") %></p>
                    <p class="muted small-text">
                        Status: <strong><%= invitation.getStatus() %></strong>
                        <% if (invitation.getSourceGroupName() != null) { %>
                        | Group: <%= h(invitation.getSourceGroupName()) %>
                        <% } %>
                    </p>
                    <% if ("PENDING".equals(invitation.getStatus())) { %>
                    <div class="action-row">
                        <form action="<%= request.getContextPath() %>/invitations/respond" method="post" class="inline-form">
                            <input type="hidden" name="invitationId" value="<%= invitation.getId() %>">
                            <input type="hidden" name="action" value="accept">
                            <button type="submit" class="btn secondary compact-button">Accept</button>
                        </form>
                        <form action="<%= request.getContextPath() %>/invitations/respond" method="post" class="inline-form">
                            <input type="hidden" name="invitationId" value="<%= invitation.getId() %>">
                            <input type="hidden" name="action" value="reject">
                            <button type="submit" class="btn danger-outline compact-button">Reject</button>
                        </form>
                    </div>
                    <% } %>
                </div>
                <%
                    }
                }
                %>
            </div>
        </div>

        <% if (Boolean.TRUE.equals(canSendInvitations)) { %>
        <div class="table-card">
            <div class="section-header">
                <h2>Send Invitation</h2>
            </div>
            <form action="<%= request.getContextPath() %>/invitations/send" method="post" class="grid-form">
                <div>
                    <label for="eventId">Event</label>
                    <select id="eventId" name="eventId" required>
                        <option value="">Select Event</option>
                        <%
                            if (manageableEventList != null) {
                                for (Event event : manageableEventList) {
                        %>
                        <option value="<%= event.getId() %>"><%= h(event.getTitle()) %></option>
                        <%
                                }
                            }
                        %>
                    </select>
                </div>
                <div>
                    <label for="targetType">Target Type</label>
                    <select id="targetType" name="targetType" required>
                        <option value="USER">Single User</option>
                        <option value="GROUP">Group</option>
                    </select>
                </div>
                <div>
                    <label for="userId">User</label>
                    <select id="userId" name="userId">
                        <option value="">Select User</option>
                        <%
                            if (assignableUsers != null) {
                                for (User user : assignableUsers) {
                        %>
                        <option value="<%= user.getId() %>"><%= h(user.getFullName()) %> (<%= h(user.getRole()) %>)</option>
                        <%
                                }
                            }
                        %>
                    </select>
                </div>
                <div>
                    <label for="inviteGroupId">Group</label>
                    <select id="inviteGroupId" name="groupId">
                        <option value="">Select Group</option>
                        <%
                            if (manageableGroups != null) {
                                for (Group group : manageableGroups) {
                        %>
                        <option value="<%= group.getId() %>"><%= h(group.getName()) %></option>
                        <%
                                }
                            }
                        %>
                    </select>
                </div>
                <div class="full-width">
                    <label for="message">Message</label>
                    <textarea id="message" name="message" rows="3" placeholder="Optional note for the invitation"></textarea>
                </div>
                <div class="search-actions">
                    <button type="submit">Send Invitation</button>
                </div>
            </form>
        </div>
        <% } %>
    </section>

    <section class="table-card">
        <div class="section-header">
            <h2>Events</h2>
            <span class="badge"><%= eventList == null ? 0 : eventList.size() %> event(s)</span>
        </div>

        <table>
            <thead>
            <tr>
                <th>ID</th>
                <th>Title</th>
                <th>Description</th>
                <th>Date</th>
                <th>Location</th>
                <th>Groups</th>
                <th>Created By</th>
                <th>Actions</th>
            </tr>
            </thead>
            <tbody>
            <%
                if (eventList == null || eventList.isEmpty()) {
            %>
            <tr>
                <td colspan="8" class="empty-state">No events match the selected filters.</td>
            </tr>
            <% } else {
                for (Event event : eventList) {
                    boolean canEditThis = Boolean.TRUE.equals(canManageEvents) && (loggedInUser.isAdmin() || event.getCreatedBy() == loggedInUser.getId());
            %>
            <tr>
                <td><%= event.getId() %></td>
                <td><%= h(event.getTitle()) %></td>
                <td><%= h(event.getDescription()) %></td>
                <td><%= event.getEventDate() %></td>
                <td><%= h(event.getLocation()) %></td>
                <td><%= h(event.getAssignedGroupNames() != null ? event.getAssignedGroupNames() : "Not assigned") %></td>
                <td><%= h(event.getCreatedByName()) %></td>
                <td>
                    <a class="btn secondary compact-button" href="#event-<%= event.getId() %>">View Participants</a>
                    <% if (eventInteractionAccess != null && Boolean.TRUE.equals(eventInteractionAccess.get(event.getId()))) { %>
                    <a class="inline-link" href="#event-<%= event.getId() %>">Open Feedback</a>
                    <% } else { %>
                    <span class="muted">Restricted</span>
                    <% } %>
                    <% if (canEditThis) { %>
                    <a class="inline-link" href="<%= request.getContextPath() %>/events/edit?id=<%= event.getId() %>">Edit</a>
                    <form class="inline-form" action="<%= request.getContextPath() %>/events/delete" method="post">
                        <input type="hidden" name="id" value="<%= event.getId() %>">
                        <button type="submit" class="link-button" onclick="return confirm('Delete this event?');">Delete</button>
                    </form>
                    <% } %>
                </td>
            </tr>
            <%
                }
            }
            %>
            </tbody>
        </table>
    </section>

    <section class="table-card">
        <div class="section-header">
            <h2>Event Feedback</h2>
            <span class="badge"><%= eventList == null ? 0 : eventList.size() %> event(s)</span>
        </div>

        <%
            if (eventList == null || eventList.isEmpty()) {
        %>
        <p class="muted">Create or search for events to review and submit feedback.</p>
        <% } else {
            for (Event event : eventList) {
                List<Feedback> feedbackList = eventFeedback == null ? null : eventFeedback.get(event.getId());
                Feedback myFeedback = currentUserFeedback == null ? null : currentUserFeedback.get(event.getId());
                boolean canAccessInteraction = eventInteractionAccess != null && Boolean.TRUE.equals(eventInteractionAccess.get(event.getId()));
                int participantCount = participantCountByEvent != null && participantCountByEvent.get(event.getId()) != null
                        ? participantCountByEvent.get(event.getId())
                        : 0;
        %>
        <article class="interaction-card" id="event-<%= event.getId() %>">
            <div class="interaction-card-header">
                <div>
                    <h3><%= h(event.getTitle()) %></h3>
                    <p class="muted">Date: <%= event.getEventDate() %> | Location: <%= h(event.getLocation()) %></p>
                    <p class="muted"><strong>Total Participants:</strong> <%= participantCount %></p>
                </div>
                <span class="badge">Created by <%= h(event.getCreatedByName()) %></span>
            </div>

            <% if (!canAccessInteraction) { %>
            <p class="muted">Feedback is restricted to accepted invitees, assigned group members, the event creator, and admins.</p>
            <% } else { %>
            <div class="feedback-panel">
                <div class="section-header">
                    <h4>Event Feedback</h4>
                    <span class="small-text">1 to 5 stars</span>
                </div>

                <form action="<%= request.getContextPath() %>/feedback" method="post" class="feedback-form">
                    <input type="hidden" name="eventId" value="<%= event.getId() %>">
                    <label for="rating-<%= event.getId() %>">Rating</label>
                    <select id="rating-<%= event.getId() %>" name="rating" required>
                        <option value="">Select rating</option>
                        <% for (int rating = 1; rating <= 5; rating++) { %>
                        <option value="<%= rating %>" <%= myFeedback != null && myFeedback.getRating() == rating ? "selected" : "" %>>
                            <%= rating %> Star<%= rating > 1 ? "s" : "" %>
                        </option>
                        <% } %>
                    </select>

                    <label for="comment-<%= event.getId() %>">Comment</label>
                    <textarea id="comment-<%= event.getId() %>" name="comment" rows="4" placeholder="Share your feedback"><%= h(myFeedback != null && myFeedback.getComment() != null ? myFeedback.getComment() : "") %></textarea>

                    <button type="submit"><%= myFeedback == null ? "Submit Feedback" : "Update Feedback" %></button>
                </form>

                <div class="feedback-list">
                    <% if (feedbackList == null || feedbackList.isEmpty()) { %>
                    <p class="muted">No feedback available yet.</p>
                    <% } else {
                        for (Feedback feedback : feedbackList) {
                    %>
                    <div class="feedback-item">
                        <div class="chat-message-meta">
                            <strong><%= h(feedback.getUserName()) %></strong>
                            <span><%= feedback.getCreatedAt() %></span>
                        </div>
                        <p class="rating-text">
                            <% for (int star = 1; star <= 5; star++) { %>
                            <span><%= star <= feedback.getRating() ? "&#9733;" : "&#9734;" %></span>
                            <% } %>
                            <span class="small-text">(<%= feedback.getRating() %>/5)</span>
                        </p>
                        <p><%= h(feedback.getComment() == null || feedback.getComment().isBlank() ? "No comment provided." : feedback.getComment()) %></p>
                    </div>
                    <%
                        }
                    }
                    %>
                </div>
            </div>
            <% } %>
        </article>
        <%
            }
        }
        %>
    </section>
</div>

</body>
</html>
