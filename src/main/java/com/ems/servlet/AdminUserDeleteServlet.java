package com.ems.servlet;

import com.ems.dao.UserDao;
import com.ems.model.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;

@WebServlet("/admin/users/delete")
public class AdminUserDeleteServlet extends HttpServlet {
    private final UserDao userDao = new UserDao();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        User loggedInUser = (User) request.getSession().getAttribute("loggedInUser");

        try {
            int userId = Integer.parseInt(request.getParameter("userId"));
            userDao.deleteUser(userId, loggedInUser.getId());
            response.sendRedirect(request.getContextPath() + "/admin/panel?success=userDeleted");
        } catch (SQLException | NumberFormatException exception) {
            throw new ServletException("Unable to delete user.", exception);
        }
    }
}
