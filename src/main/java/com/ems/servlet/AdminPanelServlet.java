package com.ems.servlet;

import com.ems.dao.EventDao;
import com.ems.dao.UserDao;
import com.ems.model.Event;
import com.ems.model.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

@WebServlet("/admin/panel")
public class AdminPanelServlet extends HttpServlet {
    private final UserDao userDao = new UserDao();
    private final EventDao eventDao = new EventDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            List<User> users = userDao.getAllUsers();
            List<Event> events = eventDao.getAllEvents();
            request.setAttribute("userList", users);
            request.setAttribute("eventList", events);
            request.getRequestDispatcher("/admin-panel.jsp").forward(request, response);
        } catch (SQLException exception) {
            throw new ServletException("Unable to load admin panel.", exception);
        }
    }
}
