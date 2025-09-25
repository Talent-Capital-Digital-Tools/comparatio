package talentcapitalme.com.comparatio.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import talentcapitalme.com.comparatio.config.CustomUserDetails;
import talentcapitalme.com.comparatio.dto.LoginRequest;
import talentcapitalme.com.comparatio.dto.RegisterRequest;
import talentcapitalme.com.comparatio.dto.TokenResponse;
import talentcapitalme.com.comparatio.entity.User;
import talentcapitalme.com.comparatio.enumeration.UserRole;
import talentcapitalme.com.comparatio.exception.UnauthorizedException;
import talentcapitalme.com.comparatio.exception.ValidationException;
import talentcapitalme.com.comparatio.exception.UserAreadyExit;
import talentcapitalme.com.comparatio.repository.UserRepository;
import talentcapitalme.com.comparatio.security.Authz;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JWTService jwtService;
    private final MatrixSeederService matrixSeederService;

    /**
     * Authenticate user and generate JWT token
     */
    public TokenResponse login(LoginRequest request) {
        try {
            // Authenticate user
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );

            // Generate JWT token
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            String token = jwtService.generateToken(userDetails);

            return new TokenResponse(token);
        } catch (AuthenticationException e) {
            throw new UnauthorizedException("Invalid email or password");
        }
    }

    /**
     * Register a new user - only admins can create users
     */
    public User registerUser(RegisterRequest request) {
        // Check if current user has admin permissions
        Authz.requireUserManagementPermission();

        // Check if user already exists
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new UserAreadyExit("User with email " + request.getEmail() + " already exists");
        }

        // Create new user
        User newUser = new User();
        newUser.setEmail(request.getEmail());
        newUser.setUsername(request.getUsername());
        newUser.setFullName(request.getFullName());
        newUser.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        newUser.setRole(request.getRole());
        newUser.setIndustry(request.getIndustry());
        
        // Set client fields for CLIENT_ADMIN users
        if (request.getRole() == UserRole.CLIENT_ADMIN) {
            newUser.setName(request.getName()); // Company name
            newUser.setActive(true); // New client admins are active by default
        }

        // Validate role assignment based on current user's permissions
        validateRoleAssignment(request.getRole(), request.getName());

        User saved = userRepository.save(newUser);

        // Seed default matrices for CLIENT_ADMIN users using their user ID
        if (saved.getRole() == UserRole.CLIENT_ADMIN) {
            try {
                matrixSeederService.seedDefaultsForClient(saved.getId());
            } catch (ValidationException ignored) {
                // Matrices already exist; safe to ignore
            }
        }

        return saved;
    }

    /**
     * Validate if current user can assign the requested role
     */
    private void validateRoleAssignment(UserRole requestedRole, String name) {
        UserRole currentUserRole = Authz.getCurrentUserRole();

        switch (currentUserRole) {
            case SUPER_ADMIN:
                // Super admin can assign any of the two roles
                break;
            case CLIENT_ADMIN:
                // Client admins cannot create SUPER_ADMIN users
                if (requestedRole == UserRole.SUPER_ADMIN) {
                    throw new UnauthorizedException("You cannot create users with SUPER_ADMIN role");
                }
                // Must specify company name for client admin users
                if (requestedRole == UserRole.CLIENT_ADMIN && (name == null || name.trim().isEmpty())) {
                    throw new UnauthorizedException("Company name is required for CLIENT_ADMIN users");
                }
                break;
            default:
                throw new UnauthorizedException("Insufficient permissions to create users");
        }
    }
}