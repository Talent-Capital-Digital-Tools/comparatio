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

        // Check if user already exists by email
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new UserAreadyExit("User with email " + request.getEmail() + " already exists");
        }
        
        // Check if username already exists
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new UserAreadyExit("User with username " + request.getUsername() + " already exists");
        }

        // Create new user
        User newUser = new User();
        newUser.setEmail(request.getEmail());
        newUser.setUsername(request.getUsername());
        newUser.setFullName(request.getFullName());
        newUser.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        newUser.setRole(request.getRole());
        newUser.setIndustry(request.getIndustry());
        newUser.setAvatarUrl(request.getAvatarUrl());
        
        // Set all fields from request
        newUser.setName(request.getName());
        newUser.setActive(request.getActive() != null ? request.getActive() : true);

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
     * Register initial admin user - no authentication required
     * This is used for the first SUPER_ADMIN user creation
     */
    public User registerInitialAdmin(RegisterRequest request) {
        // Check if any admin users already exist
        if (userRepository.findByRole(talentcapitalme.com.comparatio.enumeration.UserRole.SUPER_ADMIN).size() > 0) {
            throw new talentcapitalme.com.comparatio.exception.BadRequestException("Initial admin already exists. Use regular registration endpoint.");
        }

        // Check if user already exists by email
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new UserAreadyExit("User with email " + request.getEmail() + " already exists");
        }
        
        // Check if username already exists
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new UserAreadyExit("User with username " + request.getUsername() + " already exists");
        }

        // Create new user
        User newUser = new User();
        newUser.setEmail(request.getEmail());
        newUser.setUsername(request.getUsername());
        newUser.setFullName(request.getFullName());
        newUser.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        newUser.setRole(request.getRole());
        newUser.setIndustry(request.getIndustry());
        newUser.setAvatarUrl(request.getAvatarUrl());
        
        // Set all fields from request
        newUser.setName(request.getName());
        newUser.setActive(request.getActive() != null ? request.getActive() : true);

        User saved = userRepository.save(newUser);
        return saved;
    }

    /**
     * Validate if current user can assign the requested role
     */
    private void validateRoleAssignment(UserRole requestedRole, String name) {
        UserRole currentUserRole = Authz.getCurrentUserRole();

        switch (currentUserRole) {
            case SUPER_ADMIN:
                // Super admin can assign any role
                break;
            case CLIENT_ADMIN:
                // Client admins cannot create SUPER_ADMIN users
                if (requestedRole == UserRole.SUPER_ADMIN) {
                    throw new UnauthorizedException("You cannot create users with SUPER_ADMIN role");
                }
                // For CLIENT_ADMIN users, name (company name) is required
                if (requestedRole == UserRole.CLIENT_ADMIN && (name == null || name.trim().isEmpty())) {
                    throw new UnauthorizedException("Company name is required for CLIENT_ADMIN users");
                }
                break;
            default:
                throw new UnauthorizedException("Insufficient permissions to create users");
        }
    }
}