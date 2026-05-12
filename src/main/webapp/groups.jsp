<%@ page import="java.util.List" %>
<%@ page import="com.ems.model.Group" %>
<%@ page import="com.ems.model.User" %>
<%@ page import="com.ems.util.HtmlUtil" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%! 
    private String h(String value) { return HtmlUtil.escape(value); }
%>
<%
    User loggedInUser = (User) session.getAttribute("loggedInUser");
    List<Group> groupList = (List<Group>) request.getAttribute("groupList");
    List<User> userList = (List<User>) request.getAttribute("userList");
    Boolean canManageGroups = (Boolean) request.getAttribute("canManageGroups");
    String success = request.getParameter("success");
    String error = request.getParameter("error");
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Groups - Event Management System</title>
    <link rel="stylesheet" href="<%= request.getContextPath() %>/css/style.css">
</head>
<body class="dashboard-body">
<div class="page-container">
    <header class="topbar">
        <div>
            <h1>Groups</h1>
            <p>Manage group membership and collaboration in one place.</p>
        </div>
        <div class="topbar-actions">
            <a class="btn secondary" href="<%= request.getContextPath() %>/dashboard">Back to Dashboard</a>
        </div>
    </header>

    <% if ("created".equals(success)) { %>
    <div class="message success">Group created successfully.</div>
    <% } else if ("memberAdded".equals(success)) { %>
    <div class="message success">Member added to group.</div>
    <% } else if ("memberRemoved".equals(success)) { %>
    <div class="message success">Member removed from group.</div>
    <% } else if ("deleted".equals(success)) { %>
    <div class="message success">Group deleted successfully.</div>
    <% } %>

    <% if ("unauthorized".equals(error)) { %>
    <div class="message error">You cannot manage that group.</div>
    <% } else if ("invalidAction".equals(error)) { %>
    <div class="message error">Please enter valid group details.</div>
    <% } else if ("duplicateGroup".equals(error)) { %>
    <div class="message error">A group with that name already exists.</div>
    <% } else if ("memberExists".equals(error)) { %>
    <div class="message error">That user is already a member of the selected group.</div>
    <% } else if ("memberNotFound".equals(error)) { %>
    <div class="message error">That user was not found in the selected group.</div>
    <% } else if ("groupCreateFailed".equals(error)) { %>
    <div class="message error">Unable to create the group right now. Try again.</div>
    <% } else if ("groupDeleteFailed".equals(error)) { %>
    <div class="message error">Unable to delete that group right now.</div>
    <% } %>

    <% if (Boolean.TRUE.equals(canManageGroups)) { %>
    <section class="panel-grid">
        <div class="form-card">
            <div class="section-header">
                <h2>Create Group</h2>
            </div>
            <form action="<%= request.getContextPath() %>/groups/manage" method="post">
                <input type="hidden" name="action" value="create">

                <label for="name">Group Name</label>
                <input type="text" id="name" name="name" required placeholder="Enter group name">

                <label for="description">Description</label>
                <textarea id="description" name="description" rows="4" required placeholder="Describe the purpose of the group"></textarea>

                <button type="submit">Create Group</button>
            </form>
        </div>

        <div class="table-card">
            <div class="section-header">
                <h2>Group Management</h2>
            </div>
            <p class="muted">Open any group below to review members. Group creators and admins can add or remove members from the same screen.</p>
        </div>
    </section>
    <% } %>

    <section class="table-card">
        <div class="section-header">
            <h2>Available Groups</h2>
            <span class="badge"><%= groupList == null ? 0 : groupList.size() %> group(s)</span>
        </div>

        <%
            if (groupList == null || groupList.isEmpty()) {
        %>
        <p class="muted">No groups available for this account.</p>
        <% } else {
            for (Group group : groupList) {
                boolean canManageThisGroup = loggedInUser != null && (loggedInUser.isAdmin() || group.getCreatedBy() == loggedInUser.getId());
        %>
        <article class="interaction-card" id="group-<%= group.getId() %>">
            <div class="interaction-card-header">
                <div>
                    <h3><%= h(group.getName()) %></h3>
                    <p><%= h(group.getDescription()) %></p>
                </div>
                <div class="topbar-actions">
                    <span class="badge">Created by <%= h(group.getCreatedByName()) %></span>
                    <% if (canManageThisGroup) { %>
                    <form action="<%= request.getContextPath() %>/groups/manage" method="post" class="inline-form">
                        <input type="hidden" name="action" value="deleteGroup">
                        <input type="hidden" name="groupId" value="<%= group.getId() %>">
                        <button type="submit" class="btn danger-outline compact-button" onclick="return confirm('Delete this group?');">Delete Group</button>
                    </form>
                    <% } %>
                </div>
            </div>

            <div class="member-tags">
                <%
                    if (group.getMembers() == null || group.getMembers().isEmpty()) {
                %>
                <span class="muted">No members yet.</span>
                <% } else {
                    for (User member : group.getMembers()) {
                %>
                <span class="tag"><%= h(member.getFullName()) %> (<%= h(member.getRole()) %>)</span>
                <% if (canManageThisGroup) { %>
                <form action="<%= request.getContextPath() %>/groups/manage" method="post" class="inline-form">
                    <input type="hidden" name="action" value="removeMember">
                    <input type="hidden" name="groupId" value="<%= group.getId() %>">
                    <input type="hidden" name="userId" value="<%= member.getId() %>">
                    <button type="submit" class="link-button">Remove</button>
                </form>
                <% } %>
                <%
                    }
                }
                %>
            </div>

            <% if (canManageThisGroup) { %>
            <div class="feedback-panel">
                <div class="section-header">
                    <h4>Manage Members</h4>
                </div>
                <form action="<%= request.getContextPath() %>/groups/manage" method="post" class="inline-stack">
                    <input type="hidden" name="action" value="addMember">
                    <input type="hidden" name="groupId" value="<%= group.getId() %>">

                    <label for="user-<%= group.getId() %>">Add Member</label>
                    <select id="user-<%= group.getId() %>" name="userId" required>
                        <option value="">Select user</option>
                        <%
                            if (userList != null) {
                                for (User user : userList) {
                        %>
                        <option value="<%= user.getId() %>"><%= h(user.getFullName()) %> (<%= h(user.getRole()) %>)</option>
                        <%
                                }
                            }
                        %>
                    </select>

                    <button type="submit" class="btn secondary">Add Member</button>
                </form>
            </div>
            <% } else { %>
            <div class="feedback-panel">
                <div class="section-header">
                    <h4>Membership</h4>
                </div>
                <p class="muted">You can view the group membership here. Member changes are reserved for the group creator or an admin.</p>
            </div>
            <% } %>
        </article>
        <%
            }
        }
        %>
    </section>
</div>
</body>
</html>
