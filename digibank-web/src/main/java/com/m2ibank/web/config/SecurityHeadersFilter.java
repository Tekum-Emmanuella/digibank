package com.m2ibank.web.config;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class SecurityHeadersFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        if (response instanceof HttpServletResponse httpServletResponse) {
            httpServletResponse.setHeader("X-Content-Type-Options", "nosniff");
            httpServletResponse.setHeader("X-Frame-Options", "DENY");
            httpServletResponse.setHeader("X-XSS-Protection", "0");
            httpServletResponse.setHeader("Content-Security-Policy", "default-src 'self'; frame-ancestors 'none';");
        }
        chain.doFilter(request, response);
    }
}
