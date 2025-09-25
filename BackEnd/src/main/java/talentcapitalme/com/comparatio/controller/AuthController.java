package talentcapitalme.com.comparatio.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
import talentcapitalme.com.comparatio.service.AuthService;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@Valid @RequestBody LoginRequest request) {
        TokenResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/register")
    public ResponseEntity<User> register(@Valid @RequestBody RegisterRequest request) {
        User user = authService.registerUser(request);
        // Remove password hash from response
        user.setPasswordHash(null);
        return ResponseEntity.status(HttpStatus.CREATED).body(user);
    }

    @PostMapping("/logout")
    public ResponseEntity<java.util.Map<String, String>> logout() {
        // Stateless JWT: client should discard the token. Return a friendly message.
        return ResponseEntity.ok(java.util.Map.of("message", "Logout successful"));
    }
}
