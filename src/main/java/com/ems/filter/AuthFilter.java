package com.ems.filter;

import com.ems.model.User;
import com.ems.util.RoleUtil;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

@WebFilter(urlPatterns = {"/dashboard", "/events/*", "/groups", "/groups/*", "/invitations/*", "/notifications/*", "/feedback", "/admin/*"})
public class AuthFilter implements Filter {
    @Override
    public void init(FilterConfig filterConfig) {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        HttpServletResponse httpServletResponse = (HttpServletResponse) response;
        HttpSession session = httpServletRequest.getSession(false);

        // Block protected URLs when the user session is missing.
        User user = session == null ? null : (User) session.getAttribute("loggedInUser");
        if (user == null) {
            if (isAjaxRequest(httpServletRequest)) {
                httpServletResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Please login again.");
            } else {
                httpServletResponse.sendRedirect(httpServletRequest.getContextPath() + "/login");
            }
            return;
        }

        String uri = httpServletRequest.getRequestURI();
        if (uri.contains("/admin/") && !RoleUtil.isAdmin(user)) {
            if (isAjaxRequest(httpServletRequest)) {
                httpServletResponse.sendError(HttpServletResponse.SC_FORBIDDEN, "You do not have permission for this action.");
            } else {
                httpServletResponse.sendRedirect(httpServletRequest.getContextPath() + "/dashboard?error=unauthorized");
            }
            return;
        }

        boolean roleProtectedPath = uri.contains("/events/create")
                || uri.contains("/events/edit")
                || uri.contains("/events/update")
                || uri.contains("/events/delete")
                || uri.contains("/groups/manage")
                || uri.contains("/invitations/send");

        if (roleProtectedPath && !(RoleUtil.canManageEvents(user) || RoleUtil.canManageGroups(user) || RoleUtil.canSendInvitations(user))) {
            if (isAjaxRequest(httpServletRequest)) {
                httpServletResponse.sendError(HttpServletResponse.SC_FORBIDDEN, "You do not have permission for this action.");
            } else {
                httpServletResponse.sendRedirect(httpServletRequest.getContextPath() + "/dashboard?error=unauthorized");
            }
            return;
        }

        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
    }

    private boolean isAjaxRequest(HttpServletRequest request) {
        return "XMLHttpRequest".equalsIgnoreCase(request.getHeader("X-Requested-With"));
    }
}
