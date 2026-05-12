package com.ems.servlet;

import com.ems.dao.NotificationDao;
import com.ems.model.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;

@WebServlet("/notifications/read")
public class NotificationReadServlet extends HttpServlet {
    private final NotificationDao notificationDao = new NotificationDao();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        User loggedInUser = (User) request.getSession().getAttribute("loggedInUser");
        String action = request.getParameter("action");

        try {
            if ("clearAll".equals(action)) {
                notificationDao.clearAllNotifications(loggedInUser.getId());
                response.sendRedirect(request.getContextPath() + "/dashboard?success=notificationsCleared");
                return;
            }

            notificationDao.markAllAsRead(loggedInUser.getId());
            response.sendRedirect(request.getContextPath() + "/dashboard?success=notificationsRead");
        } catch (SQLException exception) {
            throw new ServletException("Unable to update notifications.", exception);
        }
    }
}
