package talentcapitalme.com.comparatio.config.logging;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import talentcapitalme.com.comparatio.util.SecurityUtils;

import java.io.IOException;
import java.util.Enumeration;

/**
 * Request/Response logging filter for monitoring API calls
 * Logs incoming requests and outgoing responses for debugging and monitoring
 */
@Slf4j
@Component
@Order(1)
public class RequestLoggingFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        
        long startTime = System.currentTimeMillis();
        
        // Log incoming request
        logIncomingRequest(httpRequest);
        
        try {
            chain.doFilter(request, response);
        } finally {
            // Log outgoing response
            long duration = System.currentTimeMillis() - startTime;
            logOutgoingResponse(httpRequest, httpResponse, duration);
        }
    }

    private void logIncomingRequest(HttpServletRequest request) {
        if (shouldLogRequest(request)) {
            String userId = SecurityUtils.getCurrentUserId();
            String clientId = SecurityUtils.getCurrentUserClientId();
            
            log.info("Incoming Request - Method: {}, URI: {}, User: {}, Client: {}, IP: {}, UserAgent: {}",
                    request.getMethod(),
                    request.getRequestURI(),
                    userId != null ? userId : "anonymous",
                    clientId != null ? clientId : "none",
                    getClientIpAddress(request),
                    request.getHeader("User-Agent"));
            
            // Log request headers if debug level
            if (log.isDebugEnabled()) {
                logRequestHeaders(request);
            }
        }
    }

    private void logOutgoingResponse(HttpServletRequest request, HttpServletResponse response, long duration) {
        if (shouldLogRequest(request)) {
            String userId = SecurityUtils.getCurrentUserId();
            String clientId = SecurityUtils.getCurrentUserClientId();
            
            log.info("Outgoing Response - Method: {}, URI: {}, Status: {}, Duration: {}ms, User: {}, Client: {}",
                    request.getMethod(),
                    request.getRequestURI(),
                    response.getStatus(),
                    duration,
                    userId != null ? userId : "anonymous",
                    clientId != null ? clientId : "none");
            
            // Log slow requests
            if (duration > 1000) { // More than 1 second
                log.warn("Slow Request - Method: {}, URI: {}, Duration: {}ms, User: {}, Client: {}",
                        request.getMethod(),
                        request.getRequestURI(),
                        duration,
                        userId != null ? userId : "anonymous",
                        clientId != null ? clientId : "none");
            }
        }
    }

    private boolean shouldLogRequest(HttpServletRequest request) {
        String uri = request.getRequestURI();
        
        // Skip logging for static resources and health checks
        return !uri.startsWith("/static/") &&
               !uri.startsWith("/css/") &&
               !uri.startsWith("/js/") &&
               !uri.startsWith("/images/") &&
               !uri.startsWith("/actuator/health") &&
               !uri.startsWith("/favicon.ico");
    }

    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (StringUtils.hasText(xForwardedFor)) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (StringUtils.hasText(xRealIp)) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }

    private void logRequestHeaders(HttpServletRequest request) {
        log.debug("Request Headers for {} {}:",
                request.getMethod(),
                request.getRequestURI());
        
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            String headerValue = request.getHeader(headerName);
            
            // Mask sensitive headers
            if (isSensitiveHeader(headerName)) {
                headerValue = "***MASKED***";
            }
            
            log.debug("  {}: {}", headerName, headerValue);
        }
    }

    private boolean isSensitiveHeader(String headerName) {
        String lowerHeaderName = headerName.toLowerCase();
        return lowerHeaderName.contains("authorization") ||
               lowerHeaderName.contains("cookie") ||
               lowerHeaderName.contains("x-api-key");
    }
}
