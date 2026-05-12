<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Register - Event Management System</title>
    <link rel="stylesheet" href="<%= request.getContextPath() %>/css/style.css">
</head>
<body class="auth-body">
<div class="auth-card">
    <h1>Create Account</h1>
    <p class="subtitle">Register as a member or organizer. Admin accounts are managed separately.</p>

    <%
        String errorMessage = (String) request.getAttribute("errorMessage");
        if (errorMessage != null) {
    %>
    <div class="message error"><%= errorMessage %></div>
    <% } %>

    <form action="<%= request.getContextPath() %>/register" method="post">
        <label for="fullName">Full Name</label>
        <input type="text" id="fullName" name="fullName" placeholder="Enter your full name" required>

        <label for="email">Email</label>
        <input type="email" id="email" name="email" placeholder="Enter your email" required>

        <label for="password">Password</label>
        <input type="password" id="password" name="password" placeholder="Create a password" required>

        <label for="role">Role</label>
        <select id="role" name="role" required>
            <option value="MEMBER">Member</option>
            <option value="ORGANIZER">Organizer</option>
        </select>

        <button type="submit">Register</button>
    </form>

    <p class="switch-link">Already registered? <a href="<%= request.getContextPath() %>/login">Login here</a></p>
</div>
</body>
</html>
