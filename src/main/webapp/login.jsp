<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Login - Event Management System</title>
    <link rel="stylesheet" href="<%= request.getContextPath() %>/css/style.css">
</head>
<body class="auth-body">
<div class="auth-card">
    <h1>Event Management System</h1>
    <p class="subtitle">Login to manage college events.</p>

    <%
        String success = request.getParameter("success");
        String errorMessage = (String) request.getAttribute("errorMessage");
        if ("registered".equals(success)) {
    %>
    <div class="message success">Registration successful. Please login.</div>
    <% } else if ("logout".equals(success)) { %>
    <div class="message success">You have logged out successfully.</div>
    <% } %>

    <% if (errorMessage != null) { %>
    <div class="message error"><%= errorMessage %></div>
    <% } %>

    <form action="<%= request.getContextPath() %>/login" method="post">
        <label for="email">Email</label>
        <input type="email" id="email" name="email" placeholder="Enter your email" required>

        <label for="password">Password</label>
        <input type="password" id="password" name="password" placeholder="Enter your password" required>

        <button type="submit">Login</button>
    </form>

    <p class="switch-link">New user? <a href="<%= request.getContextPath() %>/register">Create an account</a></p>
</div>
</body>
</html>
