package talentcapitalme.com.comparatio.controller;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import talentcapitalme.com.comparatio.enumeration.UserRole;
import talentcapitalme.com.comparatio.security.JwtUtil;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final JwtUtil jwtUtil;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest req) {
        // TODO: validate user credentials from DB/IdP. This is a simplified demo.
        String token = jwtUtil.generateToken(req.getUserId(), req.getRole(), req.getClientId());
        return ResponseEntity.ok(new TokenResponse(token));
    }

    @Data
    public static class LoginRequest {
        private String userId;
        private UserRole role;
        private String clientId;
    }

    @Data
    public static class TokenResponse {
        private final String token;
    }
}
