package com.tfg.cryptoosint.controller;

import com.tfg.cryptoosint.dto.GraphDTO;
import com.tfg.cryptoosint.service.TraceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Trace", description = "Operaciones para trazado de wallets")
@RestController
@RequestMapping("/api/trace")
public class TraceController {

    private final TraceService traceService;

    public TraceController(TraceService traceService) {
        this.traceService = traceService;
    }

    /**
     * Traza las transacciones asociadas a una wallet hasta una profundidad determinada.
     *
     * @param wallet dirección de la wallet a analizar
     * @param depth profundidad máxima de trazado; valor por defecto: 2
     * @return resultado del trazado generado por el servicio
     */
    @Operation(
            summary = "Traza transacciones de una wallet",
            description = "Analiza las transacciones asociadas a una wallet hasta la profundidad indicada"
    )
    @ApiResponse(responseCode = "200", description = "Trazado generado correctamente")
    @GetMapping("/{wallet}")
    public GraphDTO traceWallet(
            @Parameter(description = "Dirección de la wallet a analizar", required = true)
            @PathVariable String wallet,
            @Parameter(description = "Profundidad máxima de trazado", example = "2")
            @RequestParam(defaultValue = "2") int depth) {

        return traceService.trace(wallet, depth);
    }
}