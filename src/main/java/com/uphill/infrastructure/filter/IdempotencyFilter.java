package com.uphill.infrastructure.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@Order(1)
@RequiredArgsConstructor
public class IdempotencyFilter extends OncePerRequestFilter {

    private static final String IDEMPOTENCY_KEY_HEADER = "Idempotency-Key";
    private static final String IDEMPOTENCY_PREFIX = "idempotency:";
    private static final String PROCESSING_MARKER = "PROCESSING";
    private static final long TTL_HOURS = 24;
    private static final long TTL_SECONDS = TTL_HOURS * 60 * 60;

    private final RedisTemplate<String, String> redisTemplate;

    @Override
    protected void doFilterInternal(final HttpServletRequest request, final HttpServletResponse response, final FilterChain filterChain)
            throws ServletException, IOException {

        if (!"POST".equals(request.getMethod()) || !request.getRequestURI().equals("/api/appointments")) {
            filterChain.doFilter(request, response);
            return;
        }

        final String idempotencyKey = request.getHeader(IDEMPOTENCY_KEY_HEADER);
        if (idempotencyKey == null || idempotencyKey.trim().isEmpty()) {
            filterChain.doFilter(request, response);
            return;
        }

        final String redisKey = IDEMPOTENCY_PREFIX + idempotencyKey;

        try {
            final Boolean reserved = redisTemplate.opsForValue().setIfAbsent(redisKey, PROCESSING_MARKER, 60, TimeUnit.SECONDS);
            
            if (!Boolean.TRUE.equals(reserved)) {
                final String existingValue = redisTemplate.opsForValue().get(redisKey);
                if (PROCESSING_MARKER.equals(existingValue)) {
                    log.info("Idempotency key {} is already being processed, returning 409", idempotencyKey);
                    
                    response.setStatus(HttpStatus.CONFLICT.value());
                    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                    response.setCharacterEncoding("UTF-8");
                    
                    final String conflictMessage = "{\"message\":\"Request already in progress\"}";
                    try (PrintWriter writer = response.getWriter()) {
                        writer.write(conflictMessage);
                    }
                    return;
                } else if (existingValue != null) {
                    log.info("Idempotency key {} found in cache, returning cached response", idempotencyKey);
                    
                    response.setStatus(HttpStatus.OK.value());
                    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                    response.setCharacterEncoding("UTF-8");
                    
                    try (PrintWriter writer = response.getWriter()) {
                        writer.write(existingValue);
                    }
                    return;
                }
            }

            final ResponseBodyCaptureWrapper responseWrapper = new ResponseBodyCaptureWrapper(response);
            
            try {
                filterChain.doFilter(request, responseWrapper);
                
                final int statusCode = responseWrapper.getStatus();
                if (statusCode == HttpStatus.OK.value() || statusCode == HttpStatus.CREATED.value()) {
                    final String responseBody = responseWrapper.getResponseBody();
                    if (responseBody != null && !responseBody.trim().isEmpty()) {
                        redisTemplate.opsForValue().set(redisKey, responseBody, TTL_SECONDS, TimeUnit.SECONDS);
                        log.info("Cached successful response for idempotency key {} with TTL {} seconds", idempotencyKey, TTL_SECONDS);
                    }
                } else {
                    redisTemplate.delete(redisKey);
                    log.debug("Removed processing marker for failed idempotency key {} (status: {})", idempotencyKey, statusCode);
                }
            } catch (Exception e) {
                redisTemplate.delete(redisKey);
                log.error("Exception occurred, removed processing marker for idempotency key {}: {}", idempotencyKey, e.getMessage(), e);
                throw e;
            }

        } catch (Exception e) {
            log.error("Error in idempotency filter for key {}: {}", idempotencyKey, e.getMessage(), e);
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding("UTF-8");
            final String descriptiveMessage = String.format(
                    "{\"code\":\"IDEMPOTENCY_PROCESSING_ERROR\",\"message\":\"An unexpected error occurred while processing an idempotent request. The operation could not be completed.\",\"details\":{\"path\":\"%s\",\"idempotencyKey\":\"%s\"}}",
                    request.getRequestURI(),
                    idempotencyKey
            );
            try (PrintWriter writer = response.getWriter()) {
                writer.write(descriptiveMessage);
            }
            return;
        }
    }

}
