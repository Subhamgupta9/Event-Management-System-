package com.ems.servlet;

import com.ems.dao.UserDao;
import com.ems.model.User;
import com.ems.util.PasswordUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;

@WebServlet("/register")
public class RegisterServlet extends HttpServlet {
    private final UserDao userDao = new UserDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.getRequestDispatcher("/register.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // Read registration form values from the request.
        String fullName = request.getParameter("fullName");
        String email = request.getParameter("email");
        String password = request.getParameter("password");
        String role = request.getParameter("role");

        if (isBlank(fullName) || isBlank(email) || isBlank(password) || isBlank(role)) {
            request.setAttribute("errorMessage", "All fields are required.");
            request.getRequestDispatcher("/register.jsp").forward(request, response);
            return;
        }

        try {
            if (userDao.isEmailRegistered(email)) {
                request.setAttribute("errorMessage", "Email is already registered.");
                request.getRequestDispatcher("/register.jsp").forward(request, response);
                return;
            }

            User user = new User();
            user.setFullName(fullName.trim());
            user.setEmail(email.trim());
            user.setPassword(PasswordUtil.hashPassword(password));
            user.setRole(normalizeRole(role));

            userDao.registerUser(user);
            response.sendRedirect(request.getContextPath() + "/login?success=registered");
        } catch (SQLException exception) {
            throw new ServletException("Unable to register user.", exception);
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private String normalizeRole(String role) {
        String normalized = role == null ? "MEMBER" : role.trim().toUpperCase();
        if (!"ORGANIZER".equals(normalized) && !"MEMBER".equals(normalized)) {
            return "MEMBER";
        }
        return normalized;
    }
}
