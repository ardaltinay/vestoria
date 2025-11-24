package io.vestoria.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;

@Component
@Order(0) // Run before IdempotencyFilter (Order 1)
@RequiredArgsConstructor
public class RateLimitFilter extends OncePerRequestFilter {

  private final StringRedisTemplate redisTemplate;
  private static final int MAX_REQUESTS_PER_MINUTE = 100;
  private static final String KEY_PREFIX = "ratelimit:";

  @Override
  protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
      @NonNull FilterChain filterChain) throws ServletException, IOException {

    String clientIp = getClientIp(request);
    String key = KEY_PREFIX + clientIp;

    Long count = redisTemplate.opsForValue().increment(key);

    if (count != null && count == 1) {
      Duration ttl = Duration.ofMinutes(1);
      redisTemplate.expire(key, ttl);
    }

    if (count != null && count > MAX_REQUESTS_PER_MINUTE) {
      response.setStatus(429); // Too Many Requests
      response.setContentType("application/json");
      response.getWriter().write("{\"error\": \"Too many requests. Please try again later.\"}");
      return;
    }

    filterChain.doFilter(request, response);
  }

  private String getClientIp(HttpServletRequest request) {
    String xForwardedFor = request.getHeader("X-Forwarded-For");
    if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
      return xForwardedFor.split(",")[0].trim();
    }
    return request.getRemoteAddr();
  }
}
