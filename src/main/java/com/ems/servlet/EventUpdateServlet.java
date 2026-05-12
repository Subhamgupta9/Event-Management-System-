package com.ems.servlet;

import com.ems.dao.EventDao;
import com.ems.dao.GroupDao;
import com.ems.model.Event;
import com.ems.model.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

@WebServlet("/events/update")
public class EventUpdateServlet extends HttpServlet {
    private final EventDao eventDao = new EventDao();
    private final GroupDao groupDao = new GroupDao();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        User loggedInUser = (User) request.getSession().getAttribute("loggedInUser");

        try {
            int eventId = Integer.parseInt(request.getParameter("id"));
            Event event = buildEventFromRequest(request);
            event.setId(eventId);
            event.setCreatedBy(loggedInUser.getId());

            boolean isUpdated = eventDao.updateEvent(event, loggedInUser);
            response.sendRedirect(request.getContextPath() + "/dashboard?success=" + (isUpdated ? "updated" : "notfound"));
        } catch (IllegalArgumentException | DateTimeParseException exception) {
            Event event = new Event();
            String idValue = request.getParameter("id");
            if (idValue != null && !idValue.isBlank()) {
                try {
                    event.setId(Integer.parseInt(idValue));
                } catch (NumberFormatException ignored) {
                    event.setId(0);
                }
            }
            event.setTitle(request.getParameter("title"));
            event.setDescription(request.getParameter("description"));
            String eventDate = request.getParameter("eventDate");
            if (eventDate != null && !eventDate.isBlank()) {
                try {
                    event.setEventDate(LocalDate.parse(eventDate));
                } catch (DateTimeParseException ignored) {
                    event.setEventDate(null);
                }
            }
            event.setLocation(request.getParameter("location"));
            event.setAssignedGroupIds(parseGroupIds(request.getParameterValues("groupIds")));

            request.setAttribute("event", event);
            request.setAttribute("errorMessage", "Please enter valid event details.");
            try {
                request.setAttribute("manageableGroups", groupDao.getManageableGroups(loggedInUser));
                request.setAttribute("pageTitle", "Update Event");
                request.setAttribute("formAction", request.getContextPath() + "/events/update");
                request.getRequestDispatcher("/event-form.jsp").forward(request, response);
            } catch (SQLException sqlException) {
                throw new ServletException("Unable to reload update form.", sqlException);
            }
        } catch (SQLException exception) {
            throw new ServletException("Unable to update event.", exception);
        }
    }

    private Event buildEventFromRequest(HttpServletRequest request) {
        String title = request.getParameter("title");
        String description = request.getParameter("description");
        String eventDate = request.getParameter("eventDate");
        String location = request.getParameter("location");

        if (isBlank(title) || isBlank(description) || isBlank(eventDate) || isBlank(location)) {
            throw new IllegalArgumentException("All event fields are required.");
        }

        Event event = new Event();
        event.setTitle(title.trim());
        event.setDescription(description.trim());
        event.setEventDate(LocalDate.parse(eventDate));
        event.setLocation(location.trim());
        event.setAssignedGroupIds(parseGroupIds(request.getParameterValues("groupIds")));
        return event;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private java.util.List<Integer> parseGroupIds(String[] values) {
        java.util.List<Integer> groupIds = new java.util.ArrayList<>();
        if (values == null) {
            return groupIds;
        }
        for (String value : values) {
            try {
                groupIds.add(Integer.parseInt(value));
            } catch (NumberFormatException ignored) {
            }
        }
        return groupIds;
    }
}
