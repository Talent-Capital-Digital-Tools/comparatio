package talentcapitalme.com.comparatio.config;


import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import talentcapitalme.com.comparatio.service.JWTService;

import java.io.IOException;
@Component
public class JwtFilter extends OncePerRequestFilter {

    private  final JWTService jwtService;

    private final UserDetailsServiceImpl userDetailsService;

    public JwtFilter(JWTService jwtService, UserDetailsServiceImpl userDetailsService) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain) throws ServletException, IOException {

        // Bypass JWT processing for public endpoints
        String path = request.getRequestURI();
        if (path.startsWith("/api/auth/login")
                || path.startsWith("/api/auth/logout")
                || path.equals("/swagger-ui.html")
                || path.startsWith("/swagger-ui/")
                || path.startsWith("/v3/api-docs")
                || path.startsWith("//v3/api-docs")
                || path.startsWith("/swagger-resources/")
                || path.startsWith("/actuator/health")
                || path.startsWith("/actuator/info")) {
            filterChain.doFilter(request, response);
            return;
        }

        String autHeader = request.getHeader("Authorization");

        String token = null;
        String username = null;

        if (autHeader != null && autHeader.startsWith("Bearer ")) {
            token = autHeader.substring(7);
            try {
                username = jwtService.extractUsername(token);
            } catch (Exception ignored) {
                // Malformed/expired token: ignore and continue without authentication
            }
        }

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
            if (jwtService.validateToken(token, userDetails)) {
                UsernamePasswordAuthenticationToken authenticationToken =
                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            }
        }

        filterChain.doFilter(request, response);

    }
}

