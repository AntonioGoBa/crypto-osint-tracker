package com.tfg.cryptoosint.handler;

import com.tfg.cryptoosint.exception.BlockstreamApiException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BlockstreamApiException.class)
    public ResponseEntity<Map<String, Object>> handleBlockstreamApi(BlockstreamApiException ex) {
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                .body(Map.of(
                        "error", "BLOCKSTREAM_API_ERROR",
                        "message", ex.getMessage(),
                        "timestamp", Instant.now().toString()
                ));
    }
}
