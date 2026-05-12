package com.ems.servlet;

import com.ems.dao.UserDao;
import com.ems.model.User;
import com.ems.util.PasswordUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.SQLException;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {
    private final UserDao userDao = new UserDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session != null && session.getAttribute("loggedInUser") != null) {
            response.sendRedirect(request.getContextPath() + "/dashboard");
            return;
        }

        request.getRequestDispatcher("/login.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // Read login form values and verify them through the DAO layer.
        String email = request.getParameter("email");
        String password = request.getParameter("password");

        if (isBlank(email) || isBlank(password)) {
            request.setAttribute("errorMessage", "Email and password are required.");
            request.getRequestDispatcher("/login.jsp").forward(request, response);
            return;
        }

        try {
            User user = userDao.login(email.trim(), PasswordUtil.hashPassword(password));
            if (user == null) {
                request.setAttribute("errorMessage", "Invalid email or password.");
                request.getRequestDispatcher("/login.jsp").forward(request, response);
                return;
            }

            HttpSession session = request.getSession();
            session.setAttribute("loggedInUser", user);
            response.sendRedirect(request.getContextPath() + "/dashboard");
        } catch (SQLException exception) {
            request.setAttribute("errorMessage", buildLoginErrorMessage(exception));
            request.getRequestDispatcher("/login.jsp").forward(request, response);
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private String buildLoginErrorMessage(SQLException exception) {
        String message = exception.getMessage();
        if (message != null && message.toLowerCase().contains("database password is still set to the placeholder value")) {
            return "Database password is still using the placeholder value. Update src/main/resources/db.properties or set EMS_DB_PASSWORD, then restart the server.";
        }

        if (message != null && message.toLowerCase().contains("access denied for user")) {
            return "Database login failed. Check db.username and db.password in src/main/resources/db.properties, then restart the server.";
        }

        return "Unable to login right now because the database connection failed. Check the server database configuration and try again.";
    }
}
