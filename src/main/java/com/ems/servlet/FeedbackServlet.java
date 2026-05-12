package com.ems.servlet;

import com.ems.dao.EventDao;
import com.ems.dao.FeedbackDao;
import com.ems.model.Feedback;
import com.ems.model.User;
import com.ems.util.SqlErrorUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;

@WebServlet("/feedback")
public class FeedbackServlet extends HttpServlet {
    private final FeedbackDao feedbackDao = new FeedbackDao();
    private final EventDao eventDao = new EventDao();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        User loggedInUser = (User) request.getSession().getAttribute("loggedInUser");
        if (loggedInUser == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        try {
            int eventId = parseRequiredInt(request.getParameter("eventId"));
            if (!eventDao.eventExists(eventId)) {
                response.sendRedirect(request.getContextPath() + "/dashboard?error=invalidAction");
                return;
            }
            if (!eventDao.canAccessEventInteraction(eventId, loggedInUser)) {
                response.sendRedirect(request.getContextPath() + "/dashboard?error=unauthorized#event-" + eventId);
                return;
            }

            int rating = parseRequiredInt(request.getParameter("rating"));
            String comment = request.getParameter("comment");
            String trimmedComment = comment == null ? "" : comment.trim();

            if (rating < 1 || rating > 5) {
                response.sendRedirect(request.getContextPath() + "/dashboard?error=invalidAction");
                return;
            }
            if (trimmedComment.length() > 500) {
                response.sendRedirect(request.getContextPath() + "/dashboard?error=feedbackTooLong#event-" + eventId);
                return;
            }

            Feedback feedback = new Feedback();
            feedback.setEventId(eventId);
            feedback.setUserId(loggedInUser.getId());
            feedback.setRating(rating);
            feedback.setComment(trimmedComment);
            if (!feedbackDao.saveOrUpdateFeedback(feedback)) {
                response.sendRedirect(request.getContextPath() + "/dashboard?error=invalidAction");
                return;
            }

            response.sendRedirect(request.getContextPath() + "/dashboard?success=feedbackSaved#event-" + eventId);
        } catch (IllegalArgumentException exception) {
            response.sendRedirect(request.getContextPath() + "/dashboard?error=invalidAction");
        } catch (SQLException exception) {
            if (SqlErrorUtil.isMissingTable(exception, "feedback")) {
                response.sendRedirect(request.getContextPath() + "/dashboard?error=feedbackUnavailable");
                return;
            }
            throw new ServletException("Unable to save feedback.", exception);
        }
    }

    private int parseRequiredInt(String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("Invalid numeric value.", exception);
        }
    }
}
