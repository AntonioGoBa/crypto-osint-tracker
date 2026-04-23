package com.tfg.cryptoosint.dto;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Respuesta estándar de error")
public class ApiErrorDTO {

    @Schema(example = "BAD_REQUEST")
    private String error;

    @Schema(example = "La wallet indicada no tiene un formato BTC valido.")
    private String message;

    @Schema(example = "2026-04-23T10:15:30Z")
    private String timestamp;

    public ApiErrorDTO() {
    }

    public ApiErrorDTO(String error, String message, String timestamp) {
        this.error = error;
        this.message = message;
        this.timestamp = timestamp;
    }


    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}