package com.babyrhythm;

import static com.babyrhythm.auth.AuthService.isLogged;
import static java.net.URLDecoder.decode;

import java.io.IOException;
import java.util.List;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class EmailAuthFilter implements Filter {

    private List<String> ignoreMappings;

    public EmailAuthFilter(List<String> ignoreMappings) {
        this.ignoreMappings = ignoreMappings;
    }

    public void destroy() {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException,
            ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        String requestURI = req.getRequestURI();
        String mapping = req.getServletPath();

        if (isLogged(req) || ignoredMapping(mapping)) {
            chain.doFilter(request, response);
        } else {
            ((HttpServletResponse) response).sendRedirect("/login?redirect=" + requestURI);
        }
    }

    private boolean ignoredMapping(String mapping) {
        for (String m : ignoreMappings) {
            if (m.endsWith("*") && m.toLowerCase().startsWith(mapping.toLowerCase())) {
                return true;
            } else if (m.equalsIgnoreCase(mapping)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void init(FilterConfig arg0) throws ServletException {
    }

}
