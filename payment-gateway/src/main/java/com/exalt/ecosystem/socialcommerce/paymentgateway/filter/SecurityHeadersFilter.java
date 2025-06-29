package com.exalt.ecosystem.socialcommerce.paymentgateway.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.logging.Logger;

/**
 * Security Headers Filter
 * 
 * Adds additional security headers to all HTTP responses
 * Complements the Spring Security headers configuration
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class SecurityHeadersFilter implements Filter {
    
    private static final Logger logger = Logger.getLogger(SecurityHeadersFilter.class.getName());
    
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        logger.info("Initializing Security Headers Filter");
    }
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) 
            throws IOException, ServletException {
        
        if (response instanceof HttpServletResponse) {
            HttpServletResponse httpResponse = (HttpServletResponse) response;
            
            // Add custom security headers
            httpResponse.setHeader("X-Permitted-Cross-Domain-Policies", "none");
            httpResponse.setHeader("X-Download-Options", "noopen");
            httpResponse.setHeader("X-Content-Duration", "0");
            
            // Cache control for sensitive data
            httpResponse.setHeader("Cache-Control", "no-store, no-cache, must-revalidate, proxy-revalidate");
            httpResponse.setHeader("Pragma", "no-cache");
            httpResponse.setHeader("Expires", "0");
            
            // Remove server information
            httpResponse.setHeader("Server", "");
            httpResponse.setHeader("X-Powered-By", "");
            
            // Add request ID for tracking
            String requestId = java.util.UUID.randomUUID().toString();
            httpResponse.setHeader("X-Request-ID", requestId);
        }
        
        chain.doFilter(request, response);
    }
    
    @Override
    public void destroy() {
        logger.info("Destroying Security Headers Filter");
    }
}