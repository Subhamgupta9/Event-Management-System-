package com.ems.servlet;

import com.ems.dao.EventDao;
import com.ems.dao.GroupDao;
import com.ems.model.Event;
import com.ems.model.User;
import com.ems.util.RoleUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;

@WebServlet("/events/edit")
public class EventEditServlet extends HttpServlet {
    private final EventDao eventDao = new EventDao();
    private final GroupDao groupDao = new GroupDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        User loggedInUser = (User) request.getSession().getAttribute("loggedInUser");
        if (!RoleUtil.canManageEvents(loggedInUser)) {
            response.sendRedirect(request.getContextPath() + "/dashboard?error=unauthorized");
            return;
        }
        int eventId;
        try {
            eventId = Integer.parseInt(request.getParameter("id"));
        } catch (NumberFormatException exception) {
            response.sendRedirect(request.getContextPath() + "/dashboard?error=unauthorized");
            return;
        }

        try {
            Event event = eventDao.getEventById(eventId);
            if (event == null || (!loggedInUser.isAdmin() && event.getCreatedBy() != loggedInUser.getId())) {
                response.sendRedirect(request.getContextPath() + "/dashboard?error=unauthorized");
                return;
            }

            request.setAttribute("event", event);
            request.setAttribute("manageableGroups", groupDao.getManageableGroups(loggedInUser));
            request.setAttribute("pageTitle", "Update Event");
            request.setAttribute("formAction", request.getContextPath() + "/events/update");
            request.getRequestDispatcher("/event-form.jsp").forward(request, response);
        } catch (SQLException exception) {
            throw new ServletException("Unable to load event for editing.", exception);
        }
    }
}
