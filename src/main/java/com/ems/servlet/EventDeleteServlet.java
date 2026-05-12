package com.ems.servlet;

import com.ems.dao.EventDao;
import com.ems.model.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;

@WebServlet("/events/delete")
public class EventDeleteServlet extends HttpServlet {
    private final EventDao eventDao = new EventDao();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        User loggedInUser = (User) request.getSession().getAttribute("loggedInUser");

        try {
            int eventId = Integer.parseInt(request.getParameter("id"));
            boolean isDeleted = eventDao.deleteEvent(eventId, loggedInUser);
            response.sendRedirect(request.getContextPath() + "/dashboard?success=" + (isDeleted ? "deleted" : "notfound"));
        } catch (NumberFormatException exception) {
            response.sendRedirect(request.getContextPath() + "/dashboard?success=notfound");
        } catch (SQLException exception) {
            throw new ServletException("Unable to delete event.", exception);
        }
    }
}
