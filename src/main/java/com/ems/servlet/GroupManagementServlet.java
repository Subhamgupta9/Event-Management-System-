package com.ems.servlet;

import com.ems.dao.GroupDao;
import com.ems.dao.UserDao;
import com.ems.model.Group;
import com.ems.model.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

@WebServlet("/groups")
public class GroupManagementServlet extends HttpServlet {
    private final GroupDao groupDao = new GroupDao();
    private final UserDao userDao = new UserDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        User loggedInUser = (User) request.getSession().getAttribute("loggedInUser");

        try {
            List<Group> groups = groupDao.getGroupsForUser(loggedInUser);
            List<User> users = loggedInUser.isMember() ? List.of() : userDao.getUsersForAssignment();

            request.setAttribute("groupList", groups);
            request.setAttribute("userList", users);
            request.setAttribute("canManageGroups", !loggedInUser.isMember());
            request.getRequestDispatcher("/groups.jsp").forward(request, response);
        } catch (SQLException exception) {
            throw new ServletException("Unable to load groups page.", exception);
        }
    }
}
