package talentcapitalme.com.comparatio.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class WebSecurityConfig {

    private final UserDetailsServiceImpl userDetailsService;
    private final JwtFilter jwtFilter;

    public WebSecurityConfig(UserDetailsServiceImpl userDetailsService,
                             JwtFilter jwtFilter) {
        this.userDetailsService = userDetailsService;
        this.jwtFilter = jwtFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        httpSecurity
                // ✅ Enable CORS
                .cors().and()
                // ✅ Disable CSRF (important for API + React frontend)
                .csrf(csrf -> csrf.disable())
                // ✅ Authorization rules
                .authorizeHttpRequests(request -> request
                        // Public endpoints
                        .requestMatchers("/api/auth/login", "/api/auth/logout").permitAll()
                        .requestMatchers(
                                "/swagger-ui/**", "/swagger-ui.html",
                                "/v3/api-docs", "/v3/api-docs/**",
                                "/swagger-resources/**"
                        ).permitAll()
                        .requestMatchers("/actuator/health", "/actuator/info").permitAll()

                        // Admin-only endpoints
                        .requestMatchers(HttpMethod.POST, "/api/auth/register")
                        .hasAnyRole("SUPER_ADMIN", "CLIENT_ADMIN")
                        .requestMatchers("/api/users/**")
                        .hasAnyRole("SUPER_ADMIN", "CLIENT_ADMIN")
                        .requestMatchers("/api/matrix/**", "/api/admin/matrix/**")
                        .hasRole("SUPER_ADMIN")
                        .requestMatchers("/api/admin/dashboard/**")
                        .hasAnyRole("SUPER_ADMIN", "CLIENT_ADMIN")
                        .requestMatchers("/api/clients/**")
                        .hasRole("SUPER_ADMIN")
                        .requestMatchers("/api/test/**")
                        .hasRole("SUPER_ADMIN")

                        // Upload history - authenticated users
                        .requestMatchers("/api/upload-history/**")
                        .hasAnyRole("SUPER_ADMIN", "CLIENT_ADMIN")

                        // Calculation & template endpoints - admin access only
                        .requestMatchers("/api/calc/**")
                        .hasAnyRole("SUPER_ADMIN", "CLIENT_ADMIN")
                        .requestMatchers("/api/template/**")
                        .hasAnyRole("SUPER_ADMIN", "CLIENT_ADMIN")

                        // All other requests
                        .anyRequest().authenticated()
                )
                // ✅ Stateless session for JWT
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return httpSecurity.build();
    }

    @Bean
    @SuppressWarnings("deprecation")
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    // ✅ CORS configuration bean
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        // Allow your React frontend
        configuration.setAllowedOrigins(List.of("http://localhost:3000"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }
}
