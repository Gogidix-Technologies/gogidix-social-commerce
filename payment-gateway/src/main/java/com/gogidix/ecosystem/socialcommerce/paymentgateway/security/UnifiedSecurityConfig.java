package com.gogidix.ecosystem.socialcommerce.paymentgateway.security;

import com.gogidix.ecosystem.socialcommerce.paymentgateway.config.SecurityHeadersConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.AuthenticationEntryPoint;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Unified Security Configuration for Payment Gateway
 * 
 * SECURITY FIX: RBAC Implementation (CVSS 9.3)
 * - Prevents privilege escalation vulnerabilities
 * - Implements method-level security across all domains
 * - Provides cross-domain authorization controls
 * - Includes comprehensive audit logging
 */
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true)
public class UnifiedSecurityConfig {
    
    private static final Logger logger = Logger.getLogger(UnifiedSecurityConfig.class.getName());
    
    @Autowired
    private CustomPermissionEvaluator permissionEvaluator;
    
    @Autowired
    private JwtDecoder jwtDecoder;
    
    /**
     * Main security filter chain with domain-aware authorization
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        logger.info("Configuring unified security filter chain with RBAC protection");
        
        http
            .cors(cors -> {}) // Uses the @Bean CorsConfigurationSource from CorsConfig.java
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // Public endpoints
                .requestMatchers("/actuator/health", "/actuator/info").permitAll()
                .requestMatchers("/api/v1/payments/methods/**").permitAll() // Payment methods info
                
                // Admin endpoints - require high-level privileges
                .requestMatchers("/api/v1/admin/**").hasAnyRole("SUPER_ADMIN", "PLATFORM_ADMIN")
                .requestMatchers("/actuator/metrics", "/actuator/prometheus").hasRole("PLATFORM_ADMIN")
                
                // Payment processing - requires authentication, method-level security handles authorization
                .requestMatchers("/api/v1/payments/**").authenticated()
                
                // All other requests require authentication
                .anyRequest().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt
                    .decoder(jwtDecoder)
                    .jwtAuthenticationConverter(customJwtAuthenticationConverter())
                )
            )
            .exceptionHandling(ex -> ex
                .accessDeniedHandler(customAccessDeniedHandler())
                .authenticationEntryPoint(customAuthenticationEntryPoint())
            );
            
        // Apply comprehensive security headers
        SecurityHeadersConfig.configureSecurityHeaders(http);
            
        return http.build();
    }
    
    /**
     * Method security expression handler with custom permission evaluator
     */
    @Bean
    public MethodSecurityExpressionHandler methodSecurityExpressionHandler() {
        DefaultMethodSecurityExpressionHandler handler = new DefaultMethodSecurityExpressionHandler();
        handler.setPermissionEvaluator(permissionEvaluator);
        return handler;
    }
    
    /**
     * Custom JWT authentication converter with domain-aware authorities
     */
    @Bean
    public JwtAuthenticationConverter customJwtAuthenticationConverter() {
        logger.info("Configuring custom JWT authentication converter with domain permissions");
        
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(jwt -> {
            Collection<GrantedAuthority> authorities = new ArrayList<>();
            
            try {
                // Extract roles from JWT
                List<String> roles = jwt.getClaimAsStringList("roles");
                if (roles != null) {
                    roles.forEach(role -> {
                        authorities.add(new SimpleGrantedAuthority("ROLE_" + role));
                        logger.fine("Added role authority: ROLE_" + role);
                    });
                }
                
                // Extract permissions from JWT
                Object permissionsObj = jwt.getClaim("permissions");
                if (permissionsObj instanceof List) {
                    List<?> permissions = (List<?>) permissionsObj;
                    permissions.forEach(permObj -> {
                        if (permObj instanceof Map) {
                            Map<?, ?> perm = (Map<?, ?>) permObj;
                            String domain = (String) perm.get("domain");
                            String resource = (String) perm.get("resource");
                            String action = (String) perm.get("action");
                            
                            if (domain != null && resource != null && action != null) {
                                String permissionAuth = String.format("PERM_%s_%s_%s", 
                                    domain.toUpperCase(), resource.toUpperCase(), action.toUpperCase());
                                authorities.add(new SimpleGrantedAuthority(permissionAuth));
                                logger.fine("Added permission authority: " + permissionAuth);
                            }
                        }
                    });
                }
                
                // Extract domain access from JWT
                List<String> domains = jwt.getClaimAsStringList("domains");
                if (domains != null) {
                    domains.forEach(domain -> {
                        authorities.add(new SimpleGrantedAuthority("DOMAIN_" + domain.toUpperCase()));
                        logger.fine("Added domain authority: DOMAIN_" + domain.toUpperCase());
                    });
                }
                
                logger.info("Successfully converted JWT to " + authorities.size() + " authorities");
                
            } catch (Exception e) {
                logger.severe("Error converting JWT authorities: " + e.getMessage());
            }
            
            return authorities;
        });
        
        return converter;
    }
    
    /**
     * Custom access denied handler with audit logging
     */
    @Bean
    public AccessDeniedHandler customAccessDeniedHandler() {
        return (request, response, accessDeniedException) -> {
            String username = request.getUserPrincipal() != null ? request.getUserPrincipal().getName() : "anonymous";
            String requestURI = request.getRequestURI();
            String method = request.getMethod();
            
            logger.warning(String.format("Access denied for user '%s' attempting %s %s: %s", 
                username, method, requestURI, accessDeniedException.getMessage()));
            
            response.setStatus(403);
            response.setContentType("application/json");
            response.getWriter().write(String.format(
                "{\"error\":\"Access Denied\",\"message\":\"Insufficient privileges for %s %s\",\"timestamp\":\"%s\"}", 
                method, requestURI, java.time.Instant.now()));
        };
    }
    
    /**
     * Custom authentication entry point with audit logging
     */
    @Bean
    public AuthenticationEntryPoint customAuthenticationEntryPoint() {
        return (request, response, authException) -> {
            String requestURI = request.getRequestURI();
            String method = request.getMethod();
            
            logger.warning(String.format("Authentication required for %s %s: %s", 
                method, requestURI, authException.getMessage()));
            
            response.setStatus(401);
            response.setContentType("application/json");
            response.getWriter().write(String.format(
                "{\"error\":\"Authentication Required\",\"message\":\"Valid authentication required for %s %s\",\"timestamp\":\"%s\"}", 
                method, requestURI, java.time.Instant.now()));
        };
    }
}