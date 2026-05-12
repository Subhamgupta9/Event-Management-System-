package com.ems.servlet;

import com.ems.dao.EventDao;
import com.ems.dao.GroupDao;
import com.ems.model.Event;
import com.ems.model.Group;
import com.ems.model.User;
import com.ems.util.RoleUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

@WebServlet("/events/create")
public class EventCreateServlet extends HttpServlet {
    private final EventDao eventDao = new EventDao();
    private final GroupDao groupDao = new GroupDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        User loggedInUser = (User) request.getSession().getAttribute("loggedInUser");
        if (!RoleUtil.canManageEvents(loggedInUser)) {
            response.sendRedirect(request.getContextPath() + "/dashboard?error=unauthorized");
            return;
        }

        try {
            List<Group> manageableGroups = groupDao.getManageableGroups(loggedInUser);
            request.setAttribute("manageableGroups", manageableGroups);
            request.setAttribute("pageTitle", "Create Event");
            request.setAttribute("formAction", request.getContextPath() + "/events/create");
            request.getRequestDispatcher("/event-form.jsp").forward(request, response);
        } catch (SQLException exception) {
            throw new ServletException("Unable to load event form.", exception);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        User loggedInUser = (User) request.getSession().getAttribute("loggedInUser");

        try {
            Event event = buildEventFromRequest(request);
            event.setCreatedBy(loggedInUser.getId());
            eventDao.createEvent(event);
            response.sendRedirect(request.getContextPath() + "/dashboard?success=created");
        } catch (IllegalArgumentException | DateTimeParseException exception) {
            Event event = new Event();
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
            request.setAttribute("errorMessage", exception.getMessage());
            try {
                request.setAttribute("manageableGroups", groupDao.getManageableGroups(loggedInUser));
                request.setAttribute("pageTitle", "Create Event");
                request.setAttribute("formAction", request.getContextPath() + "/events/create");
                request.getRequestDispatcher("/event-form.jsp").forward(request, response);
            } catch (SQLException sqlException) {
                throw new ServletException("Unable to reload event form.", sqlException);
            }
        } catch (SQLException exception) {
            throw new ServletException("Unable to create event.", exception);
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

    private List<Integer> parseGroupIds(String[] values) {
        List<Integer> groupIds = new java.util.ArrayList<>();
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
