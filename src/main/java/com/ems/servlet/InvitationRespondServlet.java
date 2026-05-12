package com.ems.servlet;

import com.ems.dao.InvitationDao;
import com.ems.dao.NotificationDao;
import com.ems.model.Invitation;
import com.ems.model.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;

@WebServlet("/invitations/respond")
public class InvitationRespondServlet extends HttpServlet {
    private final InvitationDao invitationDao = new InvitationDao();
    private final NotificationDao notificationDao = new NotificationDao();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        User loggedInUser = (User) request.getSession().getAttribute("loggedInUser");

        try {
            int invitationId = Integer.parseInt(request.getParameter("invitationId"));
            String action = request.getParameter("action");
            String status = "accept".equalsIgnoreCase(action) ? "ACCEPTED" : "REJECTED";

            boolean updated = invitationDao.updateInvitationStatus(invitationId, loggedInUser.getId(), status);
            if (updated) {
                Invitation invitation = invitationDao.getInvitationById(invitationId);
                if (invitation != null) {
                    notificationDao.createNotification(
                            invitation.getSentBy(),
                            loggedInUser.getFullName() + " " + status.toLowerCase() + " the invitation for " + invitation.getEventTitle() + ".",
                            request.getContextPath() + "/dashboard"
                    );
                }
            }

            response.sendRedirect(request.getContextPath() + "/dashboard?success=inviteUpdated");
        } catch (SQLException | NumberFormatException exception) {
            throw new ServletException("Unable to update invitation.", exception);
        }
    }
}
