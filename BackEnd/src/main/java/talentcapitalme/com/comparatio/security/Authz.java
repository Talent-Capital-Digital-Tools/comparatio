package talentcapitalme.com.comparatio.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import talentcapitalme.com.comparatio.config.CustomUserDetails;
import talentcapitalme.com.comparatio.enumeration.UserRole;
import talentcapitalme.com.comparatio.exception.UnauthorizedException;

/**
 * Authorization utility class for handling role-based access control
 */
public class Authz {
    
    /**
     * Get the current authenticated user's role
     */
    public static UserRole getCurrentUserRole() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new UnauthorizedException("User not authenticated");
        }
        
        // Extract role from authorities (format: ROLE_ADMIN -> ADMIN)
        return auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(authority -> authority.startsWith("ROLE_"))
                .map(authority -> authority.substring(5)) // Remove "ROLE_" prefix
                .map(UserRole::valueOf)
                .findFirst()
                .orElseThrow(() -> new UnauthorizedException("No valid role found"));
    }
    
    /**
     * Get the current authenticated user's client ID
     * For CLIENT_ADMIN users, this returns their user ID
     * For SUPER_ADMIN users, this returns null (they can access any client)
     */
    public static String getCurrentUserClientId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new UnauthorizedException("User not authenticated");
        }
        
        if (auth.getPrincipal() instanceof CustomUserDetails userDetails) {
            UserRole role = getCurrentUserRole();
            if (role == UserRole.CLIENT_ADMIN) {
                return userDetails.getId(); // CLIENT_ADMIN users use their own ID as client ID
            } else {
                return null; // SUPER_ADMIN users have no client restriction
            }
        }
        
        throw new UnauthorizedException("Invalid user details");
    }

    /**
     * Get the current authenticated user's id
     */
    public static String getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new UnauthorizedException("User not authenticated");
        }

        if (auth.getPrincipal() instanceof CustomUserDetails userDetails) {
            return userDetails.getId();
        }

        throw new UnauthorizedException("Invalid user details");
    }
    
    /**
     * Check if current user has admin privileges (SUPER_ADMIN or CLIENT_ADMIN)
     */
    public static boolean hasAdminRole() {
        UserRole role = getCurrentUserRole();
        return role == UserRole.SUPER_ADMIN || role == UserRole.CLIENT_ADMIN;
    }
    
    /**
     * Check if current user has client admin privileges
     */
    public static boolean hasClientAdminRole() {
        UserRole role = getCurrentUserRole();
        return role == UserRole.SUPER_ADMIN || role == UserRole.CLIENT_ADMIN;
    }
    
    /**
     * Require client scope - returns the client ID to use for operations
     * If user is SUPER_ADMIN, they can access any client (use provided clientId or throw error if null)
     * If user is CLIENT_ADMIN, they can only access their own client (their user ID)
     */
    public static String requireClientScope(String requestedClientId) {
        UserRole role = getCurrentUserRole();
        String userClientId = getCurrentUserClientId();
        
        if (role == UserRole.SUPER_ADMIN) {
            // Super admin can access any client, but must specify which one
            if (requestedClientId == null) {
                throw new UnauthorizedException("Super admin must specify client ID");
            }
            return requestedClientId;
        } else if (role == UserRole.CLIENT_ADMIN) {
            // CLIENT_ADMIN users are restricted to their own client (their user ID)
            if (userClientId == null) {
                throw new UnauthorizedException("User has no associated client");
            }
            
            // If a specific client is requested, it must match the user's client
            if (requestedClientId != null && !requestedClientId.equals(userClientId)) {
                throw new UnauthorizedException("Access denied to requested client");
            }
            
            return userClientId;
        } else {
            throw new UnauthorizedException("Insufficient permissions for client access");
        }
    }
    
    /**
     * Check if user can manage users (create, update, delete)
     */
    public static void requireUserManagementPermission() {
        if (!hasAdminRole()) {
            throw new UnauthorizedException("Only administrators can manage users");
        }
    }
}