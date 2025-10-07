package com.uphill.entrypoint.rest.auth.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.uphill.core.application.service.admin.AdminService;
import com.uphill.core.auth.TokenProvider;
import com.uphill.core.domain.Admin;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.Optional;


@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final TokenProvider tokenProvider;
    private final AdminService adminService;

    @Override
    protected void doFilterInternal(
            @NonNull final HttpServletRequest request, 
            @NonNull final HttpServletResponse response, 
            @NonNull final FilterChain filterChain) throws ServletException, IOException {
        
        try {
            final String authHeader = request.getHeader("Authorization");

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                filterChain.doFilter(request, response);
                return;
            }

            final String jwt = authHeader.substring(7);

            try {
                if (tokenProvider.validateToken(jwt)) {
                    final Long adminId = tokenProvider.getUserIdFromToken(jwt);
                    
                    if (adminId != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                        final Optional<Admin> adminOpt = adminService.findById(adminId);
                        
                        if (adminOpt.isPresent()) {
                            final Admin admin = adminOpt.get();
                            
                            // Create authentication token for admin
                            final UsernamePasswordAuthenticationToken authToken = 
                                new UsernamePasswordAuthenticationToken(
                                    admin.getEmail(), 
                                    null, 
                                    Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN"))
                                );
                            
                            SecurityContextHolder.getContext().setAuthentication(authToken);
                            log.debug("Successfully authenticated admin: {} ({})", admin.getEmail(), adminId);
                        } else {
                            log.warn("Admin with ID {} not found in database", adminId);
                        }
                    }
                }
            } catch (final Exception e) {
                log.warn("Token validation failed: {}", e.getMessage());
            }

        } catch (final RuntimeException e) {
            log.error("Unexpected error during JWT processing: {}", e.getMessage(), e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            final String descriptiveMessage = String.format(
                "{\"code\":\"AUTHENTICATION_PROCESSING_ERROR\",\"message\":\"An unexpected error occurred while processing authentication for this request.\",\"details\":{\"path\":\"%s\"}}",
                request.getRequestURI()
            );
            response.getWriter().write(descriptiveMessage);
            return;
        }

        filterChain.doFilter(request, response);
    }
}
