package talentcapitalme.com.comparatio.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import talentcapitalme.com.comparatio.security.Authz;

/**
 * Utility class for security-related operations
 * Centralizes security utilities to avoid code duplication
 */
@Slf4j
public class SecurityUtils {

    /**
     * Get current authenticated user ID safely
     */
    public static String getCurrentUserId() {
        try {
            return Authz.getCurrentUserId();
        } catch (Exception e) {
            log.warn("Failed to get current user ID: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Get current authenticated user client ID safely
     */
    public static String getCurrentUserClientId() {
        try {
            return Authz.getCurrentUserClientId();
        } catch (Exception e) {
            log.warn("Failed to get current user client ID: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Get current authentication object
     */
    public static Authentication getCurrentAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

    /**
     * Check if user is authenticated
     */
    public static boolean isAuthenticated() {
        Authentication auth = getCurrentAuthentication();
        return auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal());
    }

    /**
     * Check if user has specific role
     */
    public static boolean hasRole(String role) {
        Authentication auth = getCurrentAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return false;
        }
        
        return auth.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_" + role));
    }

    /**
     * Check if user is super admin
     */
    public static boolean isSuperAdmin() {
        return hasRole("SUPER_ADMIN");
    }

    /**
     * Check if user is client admin
     */
    public static boolean isClientAdmin() {
        return hasRole("CLIENT_ADMIN");
    }

    /**
     * Get user roles as string array
     */
    public static String[] getUserRoles() {
        Authentication auth = getCurrentAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return new String[0];
        }
        
        return auth.getAuthorities().stream()
                .map(authority -> authority.getAuthority().replace("ROLE_", ""))
                .toArray(String[]::new);
    }

    /**
     * Log security event
     */
    public static void logSecurityEvent(String event, String details) {
        String userId = getCurrentUserId();
        String clientId = getCurrentUserClientId();
        
        log.info("Security Event - User: {}, Client: {}, Event: {}, Details: {}", 
                userId, clientId, event, details);
    }

    /**
     * Log unauthorized access attempt
     */
    public static void logUnauthorizedAccess(String resource, String action) {
        String userId = getCurrentUserId();
        String clientId = getCurrentUserClientId();
        
        log.warn("Unauthorized Access Attempt - User: {}, Client: {}, Resource: {}, Action: {}", 
                userId, clientId, resource, action);
    }
}
