package talentcapitalme.com.comparatio.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import talentcapitalme.com.comparatio.dto.ProfileResponse;
import talentcapitalme.com.comparatio.dto.ProfileUpdateRequest;
import talentcapitalme.com.comparatio.dto.PerformanceRatingScaleUpdateRequest;
import talentcapitalme.com.comparatio.dto.PerformanceRatingScaleResponse;
import talentcapitalme.com.comparatio.enumeration.PerformanceRatingScale;
import talentcapitalme.com.comparatio.enumeration.Currency;
import talentcapitalme.com.comparatio.service.IFileStorageService;
import talentcapitalme.com.comparatio.service.IUserService;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Profile Management Controller
 * 
 * Purpose: Handles user profile management operations
 * - User profile retrieval and updates
 * - Profile image upload
 * - Admin profile management for clients
 */
@Slf4j
@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
@Tag(name = "Profile Management", description = "User profile and image management")
public class ProfileController {

    private final IUserService userService;
    private final IFileStorageService fileStorageService;

    @Operation(summary = "Get Current User Profile", description = "Retrieve current authenticated user's profile")
    @GetMapping
    public ResponseEntity<ProfileResponse> getCurrentUserProfile() {
        log.info("Profile Controller: Retrieving current user profile");
        ProfileResponse profile = userService.getCurrentUserProfile();
        log.info("Profile Controller: Retrieved profile for user: {}", profile.getUsername());
        return ResponseEntity.ok(profile);
    }

    @Operation(summary = "Update Current User Profile", description = "Update current authenticated user's profile")
    @PutMapping
    public ResponseEntity<ProfileResponse> updateCurrentUserProfile(@Valid @RequestBody ProfileUpdateRequest request) {
        log.info("Profile Controller: Updating current user profile");
        ProfileResponse updatedProfile = userService.updateCurrentUserProfile(request);
        log.info("Profile Controller: Profile updated successfully for user: {}", updatedProfile.getUsername());
        return ResponseEntity.ok(updatedProfile);
    }

    @Operation(summary = "Upload Profile Image", description = "Upload profile image for current user")
    @PostMapping(value = "/upload-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, String>> uploadProfileImage(@RequestParam("file") MultipartFile file) {
        log.info("Profile Controller: Uploading profile image");
        
        try {
            // Validate file type
            if (file.isEmpty()) {
                throw new IllegalArgumentException("File is empty");
            }
            
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                throw new IllegalArgumentException("File must be an image");
            }
            
            // Get current user ID
            String currentUserId = talentcapitalme.com.comparatio.security.Authz.getCurrentUserId();
            
            // Store the image
            String filePath = fileStorageService.storeProfileImage(file, currentUserId);
            
            // Update user's avatar URL
            ProfileUpdateRequest updateRequest = ProfileUpdateRequest.builder()
                    .avatarUrl(filePath)
                    .build();
            userService.updateCurrentUserProfile(updateRequest);
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "Profile image uploaded successfully");
            response.put("avatarUrl", filePath);
            
            log.info("Profile Controller: Profile image uploaded successfully for user: {}", currentUserId);
            return ResponseEntity.ok(response);
            
        } catch (IOException e) {
            log.error("Profile Controller: Error uploading profile image", e);
            throw new RuntimeException("Failed to upload profile image: " + e.getMessage());
        }
    }

    @Operation(summary = "Get User Profile by ID", description = "Retrieve user profile by ID (admin only)")
    @GetMapping("/{userId}")
    public ResponseEntity<ProfileResponse> getUserProfile(@Parameter(description = "User ID") @PathVariable String userId) {
        log.info("Profile Controller: Retrieving profile for user ID: {}", userId);
        ProfileResponse profile = userService.getUserProfile(userId);
        log.info("Profile Controller: Retrieved profile for user: {}", profile.getUsername());
        return ResponseEntity.ok(profile);
    }

    @Operation(summary = "Update User Profile by ID", description = "Update user profile by ID (admin only)")
    @PutMapping("/{userId}")
    public ResponseEntity<ProfileResponse> updateUserProfile(
            @Parameter(description = "User ID") @PathVariable String userId,
            @Valid @RequestBody ProfileUpdateRequest request) {
        log.info("Profile Controller: Updating profile for user ID: {}", userId);
        ProfileResponse updatedProfile = userService.updateUserProfile(userId, request);
        log.info("Profile Controller: Profile updated successfully for user: {}", updatedProfile.getUsername());
        return ResponseEntity.ok(updatedProfile);
    }

    @Operation(summary = "Upload Profile Image for User", description = "Upload profile image for specific user (admin only)")
    @PostMapping(value = "/{userId}/upload-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, String>> uploadUserProfileImage(
            @Parameter(description = "User ID") @PathVariable String userId,
            @RequestParam("file") MultipartFile file) {
        log.info("Profile Controller: Uploading profile image for user ID: {}", userId);
        
        try {
            // Validate file type
            if (file.isEmpty()) {
                throw new IllegalArgumentException("File is empty");
            }
            
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                throw new IllegalArgumentException("File must be an image");
            }
            
            // Store the image
            String filePath = fileStorageService.storeProfileImage(file, userId);
            
            // Update user's avatar URL
            ProfileUpdateRequest updateRequest = ProfileUpdateRequest.builder()
                    .avatarUrl(filePath)
                    .build();
            userService.updateUserProfile(userId, updateRequest);
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "Profile image uploaded successfully");
            response.put("avatarUrl", filePath);
            
            log.info("Profile Controller: Profile image uploaded successfully for user ID: {}", userId);
            return ResponseEntity.ok(response);
            
        } catch (IOException e) {
            log.error("Profile Controller: Error uploading profile image for user ID: {}", userId, e);
            throw new RuntimeException("Failed to upload profile image: " + e.getMessage());
        }
    }

    @Operation(summary = "Get Profile Images Directory Info", description = "Get information about profile images directory structure")
    @GetMapping("/directory-info")
    public ResponseEntity<Map<String, String>> getProfileDirectoryInfo() {
        log.info("Profile Controller: Getting profile directory information");
        
        Map<String, String> response = new HashMap<>();
        response.put("baseDirectory", fileStorageService.getProfileImagesBaseDirectory());
        response.put("directoryStructure", "uploads/profiles/{userId}/profile_image_{userId}_{timestamp}.{extension}");
        response.put("example", "uploads/profiles/user123/profile_image_user123_20250127_182345_123.jpg");
        
        log.info("Profile Controller: Directory info retrieved");
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get Performance Rating Scales", description = "Get available performance rating scale options")
    @GetMapping("/performance-rating-scales")
    public ResponseEntity<Map<String, Object>> getPerformanceRatingScales() {
        log.info("Profile Controller: Retrieving performance rating scale options");
        
        Map<String, Object> response = new HashMap<>();
        Map<String, Object> scales = new HashMap<>();
        
        for (PerformanceRatingScale scale : PerformanceRatingScale.values()) {
            Map<String, Object> scaleInfo = new HashMap<>();
            scaleInfo.put("maxRating", scale.getMaxRating());
            scaleInfo.put("displayName", scale.getDisplayName());
            scales.put(scale.name(), scaleInfo);
        }
        
        response.put("scales", scales);
        response.put("default", PerformanceRatingScale.getDefault().name());
        
        log.info("Profile Controller: Retrieved {} performance rating scale options", scales.size());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get Available Currencies", description = "Get available currency options with details")
    @GetMapping("/currencies")
    public ResponseEntity<Map<String, Object>> getAvailableCurrencies() {
        log.info("Profile Controller: Retrieving available currency options");
        
        Map<String, Object> response = new HashMap<>();
        Map<String, Object> currencies = new HashMap<>();
        
        for (Currency currency : Currency.values()) {
            Map<String, Object> currencyInfo = new HashMap<>();
            currencyInfo.put("code", currency.getCode());
            currencyInfo.put("symbol", currency.getSymbol());
            currencyInfo.put("shortName", currency.getShortName());
            currencyInfo.put("fullName", currency.getFullName());
            currencyInfo.put("displayName", currency.getDisplayName());
            currencies.put(currency.name(), currencyInfo);
        }
        
        response.put("currencies", currencies);
        response.put("default", Currency.getDefault().name());
        
        log.info("Profile Controller: Retrieved {} currency options", currencies.size());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get Current Performance Rating Scale", 
               description = "Get current user's performance rating scale configuration")
    @GetMapping("/performance-rating-scale")
    public ResponseEntity<PerformanceRatingScaleResponse> getCurrentPerformanceRatingScale() {
        log.info("Profile Controller: Getting current performance rating scale");
        PerformanceRatingScaleResponse response = userService.getCurrentPerformanceRatingScale();
        log.info("Profile Controller: Current performance rating scale is {}", response.getPerformanceRatingScale());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Update Performance Rating Scale", 
               description = "Update current user's performance rating scale (3-point or 5-point)")
    @PatchMapping("/performance-rating-scale")
    public ResponseEntity<PerformanceRatingScaleResponse> updatePerformanceRatingScale(
            @Valid @RequestBody PerformanceRatingScaleUpdateRequest request) {
        log.info("Profile Controller: Updating performance rating scale to {}", request.getPerformanceRatingScale());
        PerformanceRatingScaleResponse response = userService.updatePerformanceRatingScale(request);
        log.info("Profile Controller: Performance rating scale updated successfully to {}", 
                response.getPerformanceRatingScale());
        return ResponseEntity.ok(response);
    }
}
