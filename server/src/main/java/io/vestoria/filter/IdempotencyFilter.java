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
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.time.Duration;

@Component
@Order(1)
@RequiredArgsConstructor
public class IdempotencyFilter extends OncePerRequestFilter {

  private final StringRedisTemplate redisTemplate;
  private static final String KEY_PREFIX = "idempotency:";
  private static final long EXPIRY_HOURS = 24;

  @Override
  protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
      @NonNull FilterChain filterChain)
      throws ServletException, IOException {

    String idempotencyKey = request.getHeader("Idempotency-Key");

    if (idempotencyKey == null || idempotencyKey.isEmpty()) {
      filterChain.doFilter(request, response);
      return;
    }

    String redisKey = KEY_PREFIX + idempotencyKey;
    String cachedResponse = redisTemplate.opsForValue().get(redisKey);

    if (cachedResponse != null) {
      // Return cached response
      // Format: STATUS|BODY
      int separatorIndex = cachedResponse.indexOf('|');
      if (separatorIndex > 0) {
        int status = Integer.parseInt(cachedResponse.substring(0, separatorIndex));
        String body = cachedResponse.substring(separatorIndex + 1);

        response.setStatus(status);
        response.setContentType("application/json");
        response.getWriter().write(body);
        return;
      }
    }

    ContentCachingResponseWrapper responseWrapper = new ContentCachingResponseWrapper(response);

    try {
      filterChain.doFilter(request, responseWrapper);
    } finally {
      int status = responseWrapper.getStatus();
      // Cache successful responses (2xx)
      if (status >= 200 && status < 300) {
        byte[] responseArray = responseWrapper.getContentAsByteArray();
        String responseBody = new String(responseArray, responseWrapper.getCharacterEncoding());

        // Store as STATUS|BODY
        String valueToCache = status + "|" + responseBody;

        // Set if absent to handle race conditions
        @SuppressWarnings("null")
        Duration ttl = Duration.ofHours(EXPIRY_HOURS);
        redisTemplate.opsForValue().setIfAbsent(redisKey, valueToCache, ttl);
      }

      responseWrapper.copyBodyToResponse();
    }
  }
}
