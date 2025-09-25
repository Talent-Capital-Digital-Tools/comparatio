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

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class WebSecurityConfig {

    private final  UserDetailsServiceImpl userDetailsService;

    private final JwtFilter jwtFilter;


    public WebSecurityConfig(UserDetailsServiceImpl userDetailsService, JwtFilter jwtFilter) {
        this.userDetailsService = userDetailsService;
        this.jwtFilter = jwtFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {

        httpSecurity
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(request -> request
                        // Public endpoints
                        .requestMatchers("/api/auth/login", "/api/auth/logout").permitAll()
                        .requestMatchers(
                                "/swagger-ui/**",

                                "/swagger-ui.html",
                                "/v3/api-docs",
                                "/v3/api-docs/**",
                                "//v3/api-docs",
                                "//v3/api-docs/**",
                                "/swagger-resources/**"
                        ).permitAll()
                        .requestMatchers("/actuator/health", "/actuator/info").permitAll()
                        
                        // Admin only endpoints - user registration/management
                        .requestMatchers(HttpMethod.POST, "/api/auth/register").hasAnyRole("SUPER_ADMIN", "CLIENT_ADMIN")
                        .requestMatchers("/api/users/**").hasAnyRole("SUPER_ADMIN", "CLIENT_ADMIN")
                        
                        // Matrix management - requires SUPER_ADMIN only
                        .requestMatchers("/api/matrix/**").hasRole("SUPER_ADMIN")
                        .requestMatchers("/api/admin/matrix/**").hasRole("SUPER_ADMIN")
                        
                        // Client management - requires SUPER_ADMIN only
                        .requestMatchers("/api/clients/**").hasRole("SUPER_ADMIN")
                        
                        // Test endpoints - requires SUPER_ADMIN only (remove in production)
                        .requestMatchers("/api/test/**").hasRole("SUPER_ADMIN")
                        
                        // Calculation endpoints - authenticated users
                        .requestMatchers("/api/calc/**").authenticated()
                        .requestMatchers("/api/template/**").authenticated()
                        
                        // All other requests require authentication
                        .anyRequest().authenticated()
                )
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return httpSecurity.build();
    }

    @Bean
    @SuppressWarnings("deprecation")
    public AuthenticationProvider authenticationProvider(){
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return  config.getAuthenticationManager();
    }



}