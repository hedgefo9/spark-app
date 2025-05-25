package com.hedgefo9.spark.services.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Enumeration;

public class TokenAuthenticationFilter extends OncePerRequestFilter {
    private static final Logger logger = LoggerFactory.getLogger(TokenAuthenticationFilter.class);
    private static final String AUTH_COOKIE_NAME = "authToken";

    private final TokenService tokenService;
    private final UserDetailsService userDetailsService;

    public TokenAuthenticationFilter(TokenService tokenService, UserDetailsService userDetailsService) {
        this.tokenService = tokenService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String token = extractToken(request);
        logger.info("Processing request: {} with token: {}", request.getRequestURI(), token);


        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            logger.info("Header {}: {}", headerName, request.getHeader(headerName));
        }

        if (token != null && tokenService.validateToken(token)) {
            String username = tokenService.getUsernameFromToken(token);
            logger.info("Token validated for user: {}", username);

            if (username != null) {
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                logger.info("Loaded user details: {}", userDetails);

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                authentication.setDetails(token);

                SecurityContextHolder.getContext().setAuthentication(authentication);
                logger.info("Set authentication in context: {}", authentication);
            }
        } else {
            logger.info("No valid token found in request");
        }

        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        boolean shouldNotFilter = path.startsWith("/api/auth/login") ||
                path.startsWith("/api/auth/logout") ||
                path.startsWith("/api/auth/register") ||
                path.startsWith("/static/") ||
                path.startsWith("/login") ||
                path.equals("/error");
        logger.info("Request path: {}, should not filter: {}", path, shouldNotFilter);
        return shouldNotFilter;
    }

    private String extractToken(HttpServletRequest request) {

        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (AUTH_COOKIE_NAME.equals(cookie.getName())) {
                    String token = cookie.getValue();
                    logger.info("Found token in cookie: {}", token);
                    return token;
                }
            }
        }


        String bearerToken = request.getHeader("Authorization");
        logger.info("Authorization header: {}", bearerToken);
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }

        return null;
    }
}