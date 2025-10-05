package talentcapitalme.com.comparatio.config.health;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;


/**
 * Database health indicator for monitoring MongoDB connectivity
 * Provides health status for database operations
 * 
 * Note: This class is temporarily disabled until actuator dependencies are properly resolved
 * Uncomment the HealthIndicator implementation once actuator is working
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DatabaseHealthIndicator /* implements HealthIndicator */ {

    private final MongoTemplate mongoTemplate;

    // @Override
    public Object health() {
        try {
            // Test database connectivity by performing a simple operation
            mongoTemplate.getCollectionNames();
            
            log.info("Database health check passed - MongoDB is connected");
            return "UP";
                    
        } catch (Exception e) {
            log.error("Database health check failed", e);
            return "DOWN";
        }
    }
}
