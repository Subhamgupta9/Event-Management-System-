package com.ems.servlet;

import com.ems.dao.GroupDao;
import com.ems.dao.NotificationDao;
import com.ems.model.Group;
import com.ems.model.User;
import com.ems.util.SqlErrorUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;

@WebServlet("/groups/manage")
public class GroupActionServlet extends HttpServlet {
    private final GroupDao groupDao = new GroupDao();
    private final NotificationDao notificationDao = new NotificationDao();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        User loggedInUser = (User) request.getSession().getAttribute("loggedInUser");
        String action = request.getParameter("action");

        try {
            if ("create".equals(action)) {
                String name = request.getParameter("name");
                String description = request.getParameter("description");
                if (isBlank(name) || isBlank(description)) {
                    response.sendRedirect(request.getContextPath() + "/groups?error=invalidAction");
                    return;
                }

                Group group = new Group();
                group.setName(name.trim());
                group.setDescription(description.trim());
                group.setCreatedBy(loggedInUser.getId());
                if (!groupDao.createGroup(group)) {
                    response.sendRedirect(request.getContextPath() + "/groups?error=groupCreateFailed");
                    return;
                }
                response.sendRedirect(request.getContextPath() + "/groups?success=created");
                return;
            }

            int groupId = Integer.parseInt(request.getParameter("groupId"));
            if (!groupDao.canManageGroup(groupId, loggedInUser)) {
                response.sendRedirect(request.getContextPath() + "/groups?error=unauthorized");
                return;
            }

            if ("addMember".equals(action)) {
                int userId = Integer.parseInt(request.getParameter("userId"));
                if (!groupDao.addMember(groupId, userId)) {
                    response.sendRedirect(request.getContextPath() + "/groups?error=memberExists");
                    return;
                }
                notificationDao.createNotification(
                        userId,
                        "You were added to a group by " + loggedInUser.getFullName() + ".",
                        request.getContextPath() + "/groups"
                );
                response.sendRedirect(request.getContextPath() + "/groups?success=memberAdded");
                return;
            }

            if ("removeMember".equals(action)) {
                int userId = Integer.parseInt(request.getParameter("userId"));
                if (!groupDao.removeMember(groupId, userId)) {
                    response.sendRedirect(request.getContextPath() + "/groups?error=memberNotFound");
                    return;
                }
                notificationDao.createNotification(
                        userId,
                        "You were removed from a group by " + loggedInUser.getFullName() + ".",
                        request.getContextPath() + "/groups"
                );
                response.sendRedirect(request.getContextPath() + "/groups?success=memberRemoved");
                return;
            }

            if ("deleteGroup".equals(action)) {
                if (!groupDao.deleteGroup(groupId)) {
                    response.sendRedirect(request.getContextPath() + "/groups?error=groupDeleteFailed");
                    return;
                }
                response.sendRedirect(request.getContextPath() + "/groups?success=deleted");
                return;
            }

            response.sendRedirect(request.getContextPath() + "/groups?error=invalidAction");
        } catch (NumberFormatException exception) {
            response.sendRedirect(request.getContextPath() + "/groups?error=invalidAction");
        } catch (SQLException exception) {
            if (SqlErrorUtil.isDuplicateEntry(exception)) {
                response.sendRedirect(request.getContextPath() + "/groups?error=duplicateGroup");
                return;
            }
            throw new ServletException("Unable to manage group.", exception);
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
