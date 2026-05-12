package com.ems.servlet;

import com.ems.dao.EventDao;
import com.ems.dao.GroupDao;
import com.ems.dao.InvitationDao;
import com.ems.dao.NotificationDao;
import com.ems.model.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@WebServlet("/invitations/send")
public class InvitationSendServlet extends HttpServlet {
    private final EventDao eventDao = new EventDao();
    private final InvitationDao invitationDao = new InvitationDao();
    private final GroupDao groupDao = new GroupDao();
    private final NotificationDao notificationDao = new NotificationDao();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        User loggedInUser = (User) request.getSession().getAttribute("loggedInUser");

        try {
            int eventId = Integer.parseInt(request.getParameter("eventId"));
            String targetType = request.getParameter("targetType");
            String message = request.getParameter("message");

            if (!eventDao.canManageEvent(eventId, loggedInUser)) {
                response.sendRedirect(request.getContextPath() + "/dashboard?error=unauthorized");
                return;
            }

            if ("USER".equalsIgnoreCase(targetType)) {
                int userId = Integer.parseInt(request.getParameter("userId"));
                invitationDao.sendInvitationToUser(eventId, userId, null, loggedInUser.getId(), message);
                notificationDao.createNotification(userId, "You received a new event invitation.", request.getContextPath() + "/dashboard");
            } else if ("GROUP".equalsIgnoreCase(targetType)) {
                int groupId = Integer.parseInt(request.getParameter("groupId"));
                if (!groupDao.canManageGroup(groupId, loggedInUser)) {
                    response.sendRedirect(request.getContextPath() + "/dashboard?error=unauthorized");
                    return;
                }

                List<Integer> memberIds = groupDao.getMemberIdsByGroupId(groupId);
                Set<Integer> uniqueMemberIds = new LinkedHashSet<>(memberIds);
                for (Integer memberId : uniqueMemberIds) {
                    invitationDao.sendInvitationToUser(eventId, memberId, groupId, loggedInUser.getId(), message);
                    notificationDao.createNotification(memberId, "A group invitation was sent for an event.", request.getContextPath() + "/dashboard");
                }
            } else {
                response.sendRedirect(request.getContextPath() + "/dashboard?error=invalidAction");
                return;
            }

            response.sendRedirect(request.getContextPath() + "/dashboard?success=inviteSent");
        } catch (SQLException | NumberFormatException exception) {
            throw new ServletException("Unable to send invitation.", exception);
        }
    }
}
