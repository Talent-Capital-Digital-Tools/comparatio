package talentcapitalme.com.comparatio.config;

import io.micrometer.core.aop.CountedAspect;
import io.micrometer.core.aop.TimedAspect;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Metrics configuration for monitoring and observability
 * Enables application metrics collection for performance monitoring
 */
@Configuration
public class MetricsConfig {

    /**
     * Enable @Timed annotation support for measuring method execution time
     */
    @Bean
    public TimedAspect timedAspect(MeterRegistry registry) {
        return new TimedAspect(registry);
    }

    /**
     * Enable @Counted annotation support for counting method invocations
     */
    @Bean
    public CountedAspect countedAspect(MeterRegistry registry) {
        return new CountedAspect(registry);
    }
}
