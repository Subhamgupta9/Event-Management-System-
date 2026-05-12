<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
    Object loggedInUser = session.getAttribute("loggedInUser");
    if (loggedInUser != null) {
        response.sendRedirect(request.getContextPath() + "/dashboard");
    } else {
        response.sendRedirect(request.getContextPath() + "/login");
    }
%>
