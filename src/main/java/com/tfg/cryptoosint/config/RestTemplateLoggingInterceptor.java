package com.tfg.cryptoosint.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;

@Component
public class RestTemplateLoggingInterceptor implements ClientHttpRequestInterceptor {

    private static final Logger httpLogger = LoggerFactory.getLogger("http.resttemplate.outbound");
    private static final String REDACTED = "***";

    private final int maxBodyLength;

    public RestTemplateLoggingInterceptor(@Value("${http.logging.max-body-length:5000}") int maxBodyLength) {
        this.maxBodyLength = maxBodyLength;
    }

    @Override
    public @NonNull ClientHttpResponse intercept(@NonNull HttpRequest request,
                                                 @NonNull byte[] body,
                                                 @NonNull ClientHttpRequestExecution execution) throws IOException {

        Instant start = Instant.now();
        URI sanitizedUri = sanitizeUri(request.getURI());
        String requestBody = abbreviate(new String(body, StandardCharsets.UTF_8));

        httpLogger.info(
                "HTTP OUTBOUND REQUEST method={} uri={} headers={} body={}",
                request.getMethod(),
                sanitizedUri,
                sanitizeHeaders(request.getHeaders()),
                requestBody
        );

        try {
            ClientHttpResponse response = execution.execute(request, body);
            String responseBody = abbreviate(StreamUtils.copyToString(response.getBody(), StandardCharsets.UTF_8));
            long elapsedMs = Duration.between(start, Instant.now()).toMillis();

            httpLogger.info(
                    "HTTP OUTBOUND RESPONSE method={} uri={} status={} statusText={} elapsedMs={} headers={} body={}",
                    request.getMethod(),
                    sanitizedUri,
                    response.getStatusCode().value(),
                    response.getStatusText(),
                    elapsedMs,
                    sanitizeHeaders(response.getHeaders()),
                    responseBody
            );

            return response;
        } catch (IOException ex) {
            long elapsedMs = Duration.between(start, Instant.now()).toMillis();
            httpLogger.error(
                    "HTTP OUTBOUND ERROR method={} uri={} elapsedMs={} message={}",
                    request.getMethod(),
                    sanitizedUri,
                    elapsedMs,
                    ex.getMessage()
            );
            throw ex;
        }
    }

    private URI sanitizeUri(URI uri) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromUri(uri);
        builder.replaceQueryParam("key", REDACTED);
        return builder.build(true).toUri();
    }

    private HttpHeaders sanitizeHeaders(HttpHeaders headers) {
        HttpHeaders sanitized = new HttpHeaders();

        for (Map.Entry<String, List<String>> entry : headers.entrySet()) {
            if (entry.getKey().equalsIgnoreCase(HttpHeaders.AUTHORIZATION)) {
                sanitized.put(entry.getKey(), List.of(REDACTED));
            } else {
                sanitized.put(entry.getKey(), entry.getValue());
            }
        }

        return sanitized;
    }

    private String abbreviate(String body) {
        if (body == null || body.isBlank()) {
            return "<empty>";
        }

        if (maxBodyLength <= 0 || body.length() <= maxBodyLength) {
            return body;
        }

        return body.substring(0, maxBodyLength) + "... [truncated]";
    }
}

