package talentcapitalme.com.comparatio.enumeration;

/**
 * Enumeration for performance rating scale types
 * Defines whether an organization uses a 3-point or 5-point performance rating scale
 */
public enum PerformanceRatingScale {
    THREE_POINT(3, "3-Point Rating Scale"),
    FIVE_POINT(5, "5-Point Rating Scale");

    private final int maxRating;
    private final String displayName;

    PerformanceRatingScale(int maxRating, String displayName) {
        this.maxRating = maxRating;
        this.displayName = displayName;
    }

    public int getMaxRating() {
        return maxRating;
    }

    public String getDisplayName() {
        return displayName;
    }

    /**
     * Get the default performance rating scale (5-point for backward compatibility)
     */
    public static PerformanceRatingScale getDefault() {
        return FIVE_POINT;
    }

    /**
     * Convert performance rating from one scale to another
     * @param rating the original rating
     * @param fromScale the source scale
     * @param toScale the target scale
     * @return converted rating
     */
    public static int convertRating(int rating, PerformanceRatingScale fromScale, PerformanceRatingScale toScale) {
        if (fromScale == toScale) {
            return rating;
        }
        
        // Convert to percentage first, then to target scale
        double percentage = (double) rating / fromScale.getMaxRating();
        return Math.max(1, (int) Math.round(percentage * toScale.getMaxRating()));
    }
}
