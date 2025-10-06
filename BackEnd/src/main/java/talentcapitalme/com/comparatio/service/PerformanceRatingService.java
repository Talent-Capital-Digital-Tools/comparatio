package talentcapitalme.com.comparatio.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import talentcapitalme.com.comparatio.entity.User;
import talentcapitalme.com.comparatio.enumeration.PerformanceRatingScale;
import talentcapitalme.com.comparatio.repository.UserRepository;
import talentcapitalme.com.comparatio.security.Authz;

@Slf4j
@Service
@RequiredArgsConstructor
public class PerformanceRatingService {

    private final UserRepository userRepository;

    /**
     * Get the performance rating scale for the current user/client
     */
    public PerformanceRatingScale getUserPerformanceRatingScale() {
        String userId = Authz.getCurrentUserId();
        log.debug("Retrieving performance rating scale for user ID: {}", userId);
        return userRepository.findById(userId)
                .map(User::getPerformanceRatingScale)
                .orElse(PerformanceRatingScale.FIVE_POINT); // Default to 5-point scale
    }

    /**
     * Calculate performance bucket based on rating and user's scale
     *
     * @param performanceRating the performance rating (1-3 or 1-5 depending on scale)
     * @param scale the performance rating scale
     * @return performance bucket (1, 2, or 3)
     */
    public int calculatePerformanceBucket(int performanceRating, PerformanceRatingScale scale) {
        log.debug("Calculating performance bucket for rating {} using scale {}", performanceRating, scale);

        // Validate rating is within scale range
        if (!isValidPerformanceRating(performanceRating, scale)) {
            log.warn("Invalid performance rating {} for scale {}. Max rating: {}",
                    performanceRating, scale, scale.getMaxRating());
            throw new IllegalArgumentException(
                    String.format("Performance rating %d is invalid for %s scale. Valid range: 1-%d",
                            performanceRating, scale.getDisplayName(), scale.getMaxRating()));
        }

        switch (scale) {
            case THREE_POINT:
                // 3-point scale: 1→1, 2→2, 3→3 (Direct mapping)
                log.debug("Using 3-point scale: rating {} → bucket {}", performanceRating, performanceRating);
                return performanceRating;

            case FIVE_POINT:
                // 5-point scale: 1→1, 2→2, 3→2, 4→3, 5→3 (Fixed mapping)
                int bucket;
                if (performanceRating == 1) {
                    bucket = 1; // Low
                } else if (performanceRating == 2) {
                    bucket = 2; // Below Expectations
                } else if (performanceRating == 3) {
                    bucket = 2; // Below Expectations
                } else if (performanceRating == 4) {
                    bucket = 3; // High
                } else if (performanceRating == 5) {
                    bucket = 3; // High
                } else {
                    bucket = 1; // fallback
                }
                log.debug("Using 5-point scale: rating {} → bucket {}", performanceRating, bucket);
                return bucket;

            default:
                // Fallback logic for unknown scale
                log.warn("Unknown performance rating scale: {}, defaulting to 5-point logic", scale);
                if (performanceRating >= 4) return 3;
                if (performanceRating >= 3) return 2;
                return 1;
        }
    }

    /**
     * Calculate performance bucket for current user's scale
     *
     * @param performanceRating the performance rating
     * @return performance bucket (1, 2, or 3)
     */
    public int calculatePerformanceBucket(int performanceRating) {
        PerformanceRatingScale scale = getUserPerformanceRatingScale();
        log.debug("User's performance rating scale: {}", scale);
        return calculatePerformanceBucket(performanceRating, scale);
    }

    /**
     * Validate performance rating against user's scale
     *
     * @param performanceRating the rating to validate
     * @param scale the performance rating scale
     * @return true if valid, false otherwise
     */
    public boolean isValidPerformanceRating(int performanceRating, PerformanceRatingScale scale) {
        return performanceRating >= 1 && performanceRating <= scale.getMaxRating();
    }

    /**
     * Validate performance rating for current user's scale
     *
     * @param performanceRating the rating to validate
     * @return true if valid, false otherwise
     */
    public boolean isValidPerformanceRating(int performanceRating) {
        PerformanceRatingScale scale = getUserPerformanceRatingScale();
        return isValidPerformanceRating(performanceRating, scale);
    }

    /**
     * Convert performance rating to 5-point scale for storage consistency
     *
     * @param performanceRating the original rating
     * @param fromScale the source scale
     * @return converted rating on 5-point scale
     */
    public int convertToFivePointScale(int performanceRating, PerformanceRatingScale fromScale) {
        return PerformanceRatingScale.convertRating(performanceRating, fromScale, PerformanceRatingScale.FIVE_POINT);
    }

    /**
     * Convert performance rating from 5-point scale to user's scale
     *
     * @param performanceRating5 the rating on 5-point scale
     * @param toScale the target scale
     * @return converted rating on target scale
     */
    public int convertFromFivePointScale(int performanceRating5, PerformanceRatingScale toScale) {
        return PerformanceRatingScale.convertRating(performanceRating5, PerformanceRatingScale.FIVE_POINT, toScale);
    }
}
