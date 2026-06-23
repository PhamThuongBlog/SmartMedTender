package com.medbid.auth.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.medbid.auth.entity.User;
import com.medbid.auth.repository.UserRepository;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.SecurityException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private static final String BEARER_PREFIX = "Bearer ";
    private static final String AUTHORIZATION_HEADER = "Authorization";

    private static final AntPathMatcher pathMatcher = new AntPathMatcher();

    private static final List<String> SKIP_PATHS = List.of(
            "/",
            "/error",
            "/api/health",
            "/api/auth/login",
            "/api/auth/refresh",
            "/api/auth/register",
            "/actuator/**",
            "/swagger-ui/**",
            "/api-docs/**",
            "/v3/api-docs/**"
    );

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
        String path = request.getServletPath();
        return SKIP_PATHS.stream().anyMatch(pattern -> pathMatcher.match(pattern, path));
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = request.getHeader(AUTHORIZATION_HEADER);

        if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
            sendErrorResponse(response, HttpStatus.UNAUTHORIZED.value(),
                    "Missing or invalid Authorization header", "Authentication required");
            return;
        }

        String token = authHeader.substring(BEARER_PREFIX.length());

        try {
            String username = jwtUtil.extractUsername(token);

            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                User user = userRepository.findByUsername(username).orElse(null);

                if (user == null) {
                    sendErrorResponse(response, HttpStatus.UNAUTHORIZED.value(),
                            "User not found", "Invalid token");
                    return;
                }

                if (!user.isEnabled()) {
                    sendErrorResponse(response, HttpStatus.UNAUTHORIZED.value(),
                            "Account is disabled", "Account disabled");
                    return;
                }

                if (!user.isAccountNonLocked()) {
                    sendErrorResponse(response, HttpStatus.UNAUTHORIZED.value(),
                            "Account is locked", "Account locked");
                    return;
                }

                if (jwtUtil.isTokenExpired(token)) {
                    sendErrorResponse(response, HttpStatus.UNAUTHORIZED.value(),
                            "Token has expired", "Token expired");
                    return;
                }

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (ExpiredJwtException e) {
            log.debug("Expired JWT token: {}", e.getMessage());
            sendErrorResponse(response, HttpStatus.UNAUTHORIZED.value(),
                    "Token has expired", "Token expired");
            return;
        } catch (SecurityException | MalformedJwtException e) {
            log.debug("Invalid JWT token: {}", e.getMessage());
            sendErrorResponse(response, HttpStatus.UNAUTHORIZED.value(),
                    "Invalid token signature or format", "Invalid token");
            return;
        } catch (UnsupportedJwtException e) {
            log.debug("Unsupported JWT token: {}", e.getMessage());
            sendErrorResponse(response, HttpStatus.UNAUTHORIZED.value(),
                    "Unsupported token type", "Invalid token");
            return;
        } catch (IllegalArgumentException e) {
            log.debug("JWT claims string is empty: {}", e.getMessage());
            sendErrorResponse(response, HttpStatus.UNAUTHORIZED.value(),
                    "Token claims are missing or empty", "Invalid token");
            return;
        }

        filterChain.doFilter(request, response);
    }

    private void sendErrorResponse(HttpServletResponse response, int status,
                                    String message, String error) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", status);
        body.put("message", message);
        body.put("error", error);
        body.put("timestamp", LocalDateTime.now().toString());

        response.getWriter().write(objectMapper.writeValueAsString(body));
    }
}
