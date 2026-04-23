package com.tfg.cryptoosint.handler;

import com.tfg.cryptoosint.dto.ApiErrorDTO;
import com.tfg.cryptoosint.exception.BlockstreamApiException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiErrorDTO> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ApiErrorDTO(
                        "BAD_REQUEST",
                        ex.getMessage(),
                        Instant.now().toString()
                ));
    }

    @ExceptionHandler(BlockstreamApiException.class)
    public ResponseEntity<ApiErrorDTO> handleBlockstreamApi(BlockstreamApiException ex) {
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                .body(new ApiErrorDTO(
                        "BLOCKSTREAM_API_ERROR",
                        ex.getMessage(),
                        Instant.now().toString()
                ));
    }
}
