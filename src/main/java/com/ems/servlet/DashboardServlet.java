package com.ems.servlet;

import com.ems.dao.DashboardDao;
import com.ems.dao.EventDao;
import com.ems.dao.FeedbackDao;
import com.ems.dao.GroupDao;
import com.ems.dao.InvitationDao;
import com.ems.dao.NotificationDao;
import com.ems.dao.UserDao;
import com.ems.model.DashboardStats;
import com.ems.model.Event;
import com.ems.model.Feedback;
import com.ems.model.Group;
import com.ems.model.Invitation;
import com.ems.model.Notification;
import com.ems.model.User;
import com.ems.util.RoleUtil;
import com.ems.util.SqlErrorUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@WebServlet("/dashboard")
public class DashboardServlet extends HttpServlet {
    private final EventDao eventDao = new EventDao();
    private final UserDao userDao = new UserDao();
    private final GroupDao groupDao = new GroupDao();
    private final InvitationDao invitationDao = new InvitationDao();
    private final NotificationDao notificationDao = new NotificationDao();
    private final DashboardDao dashboardDao = new DashboardDao();
    private final FeedbackDao feedbackDao = new FeedbackDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        User loggedInUser = (User) request.getSession().getAttribute("loggedInUser");
        if (loggedInUser == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        try {
            String keyword = trimToNull(request.getParameter("keyword"));
            String location = trimToNull(request.getParameter("location"));
            String fromDate = normalizeDate(request.getParameter("fromDate"));
            String toDate = normalizeDate(request.getParameter("toDate"));
            Integer groupId = parseInteger(request.getParameter("groupId"));
            String validationError = null;

            if (request.getParameter("fromDate") != null && fromDate == null) {
                validationError = "Invalid From Date filter was ignored.";
            } else if (request.getParameter("toDate") != null && toDate == null) {
                validationError = "Invalid To Date filter was ignored.";
            }

            List<Event> eventList = eventDao.searchEventsForUser(loggedInUser, keyword, location, fromDate, toDate, groupId);
            List<Event> manageableEventList = eventDao.getEventsForCreator(loggedInUser);
            List<Group> manageableGroups = groupDao.getManageableGroups(loggedInUser);
            List<Group> visibleGroups = groupDao.getGroupsForUser(loggedInUser);
            List<User> assignableUsers = userDao.getUsersForAssignment();
            List<Invitation> invitations = invitationDao.getInvitationsForUser(loggedInUser.getId());
            List<Notification> notifications = notificationDao.getNotificationsForUser(loggedInUser.getId());
            DashboardStats stats = dashboardDao.getDashboardStats(loggedInUser.getId());
            Map<Integer, List<Feedback>> eventFeedback = new HashMap<>();
            Map<Integer, Feedback> currentUserFeedback = new HashMap<>();
            Map<Integer, Boolean> eventInteractionAccess = new HashMap<>();
            Map<Integer, Integer> participantCountByEvent = new HashMap<>();
            String moduleWarning = validationError;

            try {
                List<Integer> eventIds = new ArrayList<>();
                for (Event event : eventList) {
                    eventIds.add(event.getId());
                    eventInteractionAccess.put(event.getId(), true);
                }

                eventFeedback.putAll(feedbackDao.getFeedbackForEvents(eventIds));
                participantCountByEvent.putAll(eventDao.getParticipantCounts(eventIds));

                for (Integer eventId : eventIds) {
                    List<Feedback> feedbackList = eventFeedback.get(eventId);
                    if (feedbackList != null) {
                        for (Feedback feedback : feedbackList) {
                            if (feedback.getUserId() == loggedInUser.getId()) {
                                currentUserFeedback.put(eventId, feedback);
                                break;
                            }
                        }
                    }
                }
            } catch (SQLException moduleException) {
                if (SqlErrorUtil.isMissingTable(moduleException, "feedback")) {
                    moduleWarning = appendWarning(
                            moduleWarning,
                            "Feedback is unavailable. Run the latest SQL migration for the feedback table."
                    );
                } else {
                    throw moduleException;
                }
            }

            request.setAttribute("eventList", eventList);
            request.setAttribute("manageableEventList", manageableEventList);
            request.setAttribute("manageableGroups", manageableGroups);
            request.setAttribute("visibleGroups", visibleGroups);
            request.setAttribute("assignableUsers", assignableUsers);
            request.setAttribute("invitationList", invitations);
            request.setAttribute("notificationList", notifications);
            request.setAttribute("stats", stats);
            request.setAttribute("eventFeedback", eventFeedback);
            request.setAttribute("currentUserFeedback", currentUserFeedback);
            request.setAttribute("eventInteractionAccess", eventInteractionAccess);
            request.setAttribute("participantCountByEvent", participantCountByEvent);
            request.setAttribute("canManageEvents", RoleUtil.canManageEvents(loggedInUser));
            request.setAttribute("canManageGroups", RoleUtil.canManageGroups(loggedInUser));
            request.setAttribute("canSendInvitations", RoleUtil.canSendInvitations(loggedInUser));
            request.setAttribute("keyword", keyword);
            request.setAttribute("location", location);
            request.setAttribute("fromDate", fromDate);
            request.setAttribute("toDate", toDate);
            request.setAttribute("groupId", groupId);
            request.setAttribute("validationError", moduleWarning);
            request.getRequestDispatcher("/dashboard.jsp").forward(request, response);
        } catch (SQLException exception) {
            throw new ServletException("Unable to load dashboard.", exception);
        }
    }

    private Integer parseInteger(String value) {
        try {
            return value == null || value.isBlank() ? null : Integer.parseInt(value);
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    private String trimToNull(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return value.trim();
    }

    private String normalizeDate(String value) {
        String trimmed = trimToNull(value);
        if (trimmed == null) {
            return null;
        }

        try {
            return LocalDate.parse(trimmed).toString();
        } catch (DateTimeParseException exception) {
            return null;
        }
    }

    private String appendWarning(String currentWarning, String newWarning) {
        if (currentWarning == null || currentWarning.isBlank()) {
            return newWarning;
        }
        return currentWarning + " " + newWarning;
    }
}
