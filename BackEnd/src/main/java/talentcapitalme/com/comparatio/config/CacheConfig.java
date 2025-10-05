package talentcapitalme.com.comparatio.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Cache configuration for the application
 * Enables caching for frequently accessed data to improve performance
 */
@Configuration
@EnableCaching
public class CacheConfig {

    /**
     * Configure cache manager
     */
    @Bean
    public CacheManager cacheManager() {
        return new ConcurrentMapCacheManager(
                "matrices",           // Cache for adjustment matrices
                "users",              // Cache for user data
                "performanceScales",  // Cache for performance rating scales
                "clientData",         // Cache for client-specific data
                "templates"           // Cache for template data
        );
    }
}
