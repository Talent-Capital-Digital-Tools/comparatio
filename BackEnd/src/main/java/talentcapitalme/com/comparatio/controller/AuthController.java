package talentcapitalme.com.comparatio.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import talentcapitalme.com.comparatio.dto.LoginRequest;
import talentcapitalme.com.comparatio.dto.RegisterRequest;
import talentcapitalme.com.comparatio.dto.TokenResponse;
import talentcapitalme.com.comparatio.entity.User;
import talentcapitalme.com.comparatio.service.IAuthService;

/**
 * Authentication Controller
 * 
 * Purpose: Handles user authentication and registration operations
 * - User login with JWT token generation
 * - User registration with role-based access control
 * - Password validation and security
 * - Token-based authentication for API access
 */
@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "User authentication and registration")
public class AuthController {

    private final IAuthService authService;

    @Operation(summary = "User Login", description = "Authenticate user and get JWT token")
    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@Valid @RequestBody LoginRequest request) {
        log.info("Authentication Controller: Processing user login request for email: {}", request.getEmail());
        TokenResponse response = authService.login(request);
        log.info("Authentication Controller: Login successful for user: {}", request.getEmail());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "User Registration", description = "Register a new user. First user must be SUPER_ADMIN, subsequent users require admin authentication.")
    @PostMapping("/register")
    public ResponseEntity<User> register(@Valid @RequestBody RegisterRequest request) {
        log.info("Authentication Controller: Processing user registration request for email: {} with role: {}", 
                request.getEmail(), request.getRole());
        User user = authService.registerUser(request);
        // Remove password hash from response
        user.setPasswordHash(null);
        log.info("Authentication Controller: User registration successful for email: {} with ID: {}", 
                request.getEmail(), user.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(user);
    }


    @Operation(summary = "User Logout", description = "Logout user (client should discard JWT token)")
    @PostMapping("/logout")
    public ResponseEntity<java.util.Map<String, String>> logout() {
        log.info("Authentication Controller: Processing user logout request");
        // Stateless JWT: client should discard the token. Return a friendly message.
        log.info("Authentication Controller: Logout successful - token should be discarded by client");
        return ResponseEntity.ok(java.util.Map.of("message", "Logout successful"));
    }
}
