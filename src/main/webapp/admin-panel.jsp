<%@ page import="java.util.List" %>
<%@ page import="com.ems.model.User" %>
<%@ page import="com.ems.model.Event" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
    List<User> userList = (List<User>) request.getAttribute("userList");
    List<Event> eventList = (List<Event>) request.getAttribute("eventList");
    String success = request.getParameter("success");
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Admin Panel - Event Management System</title>
    <link rel="stylesheet" href="<%= request.getContextPath() %>/css/style.css">
</head>
<body class="dashboard-body">
<div class="page-container">
    <header class="topbar">
        <div>
            <h1>Admin Panel</h1>
            <p>Manage all users and all events from one place.</p>
        </div>
        <div class="topbar-actions">
            <a class="btn secondary" href="<%= request.getContextPath() %>/dashboard">Back to Dashboard</a>
        </div>
    </header>

    <% if ("userDeleted".equals(success)) { %>
    <div class="message success">User deleted successfully.</div>
    <% } %>

    <section class="table-card">
        <div class="section-header">
            <h2>All Users</h2>
        </div>
        <table>
            <thead>
            <tr>
                <th>ID</th>
                <th>Name</th>
                <th>Email</th>
                <th>Role</th>
                <th>Action</th>
            </tr>
            </thead>
            <tbody>
            <%
                if (userList != null) {
                    for (User user : userList) {
            %>
            <tr>
                <td><%= user.getId() %></td>
                <td><%= user.getFullName() %></td>
                <td><%= user.getEmail() %></td>
                <td><%= user.getRole() %></td>
                <td>
                    <form action="<%= request.getContextPath() %>/admin/users/delete" method="post" class="inline-form">
                        <input type="hidden" name="userId" value="<%= user.getId() %>">
                        <button type="submit" class="link-button" onclick="return confirm('Delete this user?');">Delete</button>
                    </form>
                </td>
            </tr>
            <%
                    }
                }
            %>
            </tbody>
        </table>
    </section>

    <section class="table-card">
        <div class="section-header">
            <h2>All Events</h2>
        </div>
        <table>
            <thead>
            <tr>
                <th>ID</th>
                <th>Title</th>
                <th>Date</th>
                <th>Location</th>
                <th>Created By</th>
                <th>Action</th>
            </tr>
            </thead>
            <tbody>
            <%
                if (eventList != null) {
                    for (Event event : eventList) {
            %>
            <tr>
                <td><%= event.getId() %></td>
                <td><%= event.getTitle() %></td>
                <td><%= event.getEventDate() %></td>
                <td><%= event.getLocation() %></td>
                <td><%= event.getCreatedByName() %></td>
                <td>
                    <a class="inline-link" href="<%= request.getContextPath() %>/events/edit?id=<%= event.getId() %>">Edit</a>
                    <form action="<%= request.getContextPath() %>/events/delete" method="post" class="inline-form">
                        <input type="hidden" name="id" value="<%= event.getId() %>">
                        <button type="submit" class="link-button" onclick="return confirm('Delete this event?');">Delete</button>
                    </form>
                </td>
            </tr>
            <%
                    }
                }
            %>
            </tbody>
        </table>
    </section>
</div>
</body>
</html>
