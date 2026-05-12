<%@ page import="com.ems.model.Event" %>
<%@ page import="com.ems.model.Group" %>
<%@ page import="java.util.List" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
    Event event = (Event) request.getAttribute("event");
    String pageTitle = (String) request.getAttribute("pageTitle");
    String formAction = (String) request.getAttribute("formAction");
    String errorMessage = (String) request.getAttribute("errorMessage");
    List<Group> manageableGroups = (List<Group>) request.getAttribute("manageableGroups");
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title><%= pageTitle %> - Event Management System</title>
    <link rel="stylesheet" href="<%= request.getContextPath() %>/css/style.css">
</head>
<body class="dashboard-body">
<div class="page-container narrow">
    <header class="topbar">
        <div>
            <h1><%= pageTitle %></h1>
            <p>Fill all details carefully and assign the event to relevant groups.</p>
        </div>
        <a class="btn secondary" href="<%= request.getContextPath() %>/dashboard">Back to Dashboard</a>
    </header>

    <div class="form-card">
        <% if (errorMessage != null) { %>
        <div class="message error"><%= errorMessage %></div>
        <% } %>

        <form action="<%= formAction %>" method="post">
            <% if (event != null) { %>
            <input type="hidden" name="id" value="<%= event.getId() %>">
            <% } %>

            <label for="title">Event Title</label>
            <input type="text" id="title" name="title" required
                   value="<%= event != null && event.getTitle() != null ? event.getTitle() : "" %>">

            <label for="description">Description</label>
            <textarea id="description" name="description" rows="5" required><%= event != null && event.getDescription() != null ? event.getDescription() : "" %></textarea>

            <label for="eventDate">Event Date</label>
            <input type="date" id="eventDate" name="eventDate" required
                   value="<%= event != null && event.getEventDate() != null ? event.getEventDate() : "" %>">

            <label for="location">Location</label>
            <input type="text" id="location" name="location" required
                   value="<%= event != null && event.getLocation() != null ? event.getLocation() : "" %>">

            <label for="groupIds">Assign Groups</label>
            <select id="groupIds" name="groupIds" multiple class="multi-select">
                <%
                    if (manageableGroups != null) {
                        for (Group group : manageableGroups) {
                            boolean selected = event != null && event.getAssignedGroupIds() != null && event.getAssignedGroupIds().contains(group.getId());
                %>
                <option value="<%= group.getId() %>" <%= selected ? "selected" : "" %>><%= group.getName() %></option>
                <%
                        }
                    }
                %>
            </select>
            <p class="helper-text">Hold Ctrl (or Cmd on Mac) to select multiple groups.</p>

            <button type="submit"><%= event == null ? "Create Event" : "Update Event" %></button>
        </form>
    </div>
</div>
</body>
</html>
