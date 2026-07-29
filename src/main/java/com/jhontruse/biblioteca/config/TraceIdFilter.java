package com.jhontruse.biblioteca.config;

import com.jhontruse.biblioteca.constants.TraceConstants;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Slf4j
@Component
public class TraceIdFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        long startTime = System.currentTimeMillis();

        try {
            // Reutiliza traceId si viene del cliente
            String traceId = request.getHeader(TraceConstants.HEADER_TRACE_ID);
            if (traceId == null || traceId.isBlank()) {
                traceId = generateTraceId();
            }

            // Guardar en MDC
            MDC.put(TraceConstants.TRACE_ID, traceId);
            MDC.put(TraceConstants.HTTP_METHOD, request.getMethod());
            MDC.put(TraceConstants.ENDPOINT, request.getRequestURI());
            MDC.put(TraceConstants.IP, getClientIp(request));
            MDC.put(TraceConstants.STATUS, String.valueOf(response.getStatus()));
            MDC.put(TraceConstants.DURATION, System.currentTimeMillis() - startTime + "ms");
            // Retornar traceId al cliente
            response.setHeader(TraceConstants.HEADER_TRACE_ID, traceId);
            filterChain.doFilter(request, response);
            MDC.clear();
        } finally {
            // MUY IMPORTANTE
            MDC.clear();
        }

    }

    private String generateTraceId() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
    }

    private String getClientIp(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null || xfHeader.isBlank()) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0];
    }
}
