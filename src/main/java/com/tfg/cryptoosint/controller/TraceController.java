package com.tfg.cryptoosint.controller;

import com.tfg.cryptoosint.dto.ApiErrorDTO;
import com.tfg.cryptoosint.dto.GraphDTO;
import com.tfg.cryptoosint.dto.WalletBlockSummaryDTO;
import com.tfg.cryptoosint.service.TraceService;
import com.tfg.cryptoosint.util.JsonParserUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

@Tag(
        name = "Trace",
        description = "Endpoints para trazado de wallets Bitcoin y consulta de bloques donde aparece una dirección"
)
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
     * @param depth  profundidad máxima de trazado; valor por defecto: 2
     * @return resultado del trazado generado por el servicio
     */
    @Operation(
            summary = "Traza transacciones de una wallet",
            description = "Genera un grafo de relaciones a partir de una wallet Bitcoin hasta la profundidad indicada. " +
                    "El resultado incluye wallet inicial, nodos y aristas con importes y txids agregados."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Trazado generado correctamente",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = GraphDTO.class),
                            examples = @ExampleObject(
                                    name = "TraceGraphExample",
                                    value = """
                                            {
                                              "initialWallet": "bc1qxy2kgdygjrsqtzq2n0yrf2493p83kkfjhx0wlh",
                                              "initialAmountSatoshis": 1250000,
                                              "initialAmountBtc": "0.01250000",
                                              "nodes": [
                                                {
                                                  "wallet": "bc1qxy2kgdygjrsqtzq2n0yrf2493p83kkfjhx0wlh",
                                                  "amountSatoshis": 1250000,
                                                  "amountBtc": "0.01250000"
                                                },
                                                {
                                                  "wallet": "bc1qrecipient0000000000000000000000000000",
                                                  "amountSatoshis": 250000,
                                                  "amountBtc": "0.00250000"
                                                },
                                                {
                                                  "wallet": "bc1qchange0000000000000000000000000000000",
                                                  "amountSatoshis": 1000000,
                                                  "amountBtc": "0.01000000"
                                                }
                                              ],
                                              "edges": [
                                                {
                                                  "from": "bc1qxy2kgdygjrsqtzq2n0yrf2493p83kkfjhx0wlh",
                                                  "to": "bc1qrecipient0000000000000000000000000000",
                                                  "amount": 250000,
                                                  "amountBtc": "0.00250000",
                                                  "txCount": 1,
                                                  "txids": [
                                                    "4d3f1f0d2a8b7c6e5f4a3b2c1d0e9f876543210abcdef1234567890abcdef12"
                                                  ]
                                                },
                                                {
                                                  "from": "bc1qxy2kgdygjrsqtzq2n0yrf2493p83kkfjhx0wlh",
                                                  "to": "bc1qchange0000000000000000000000000000000",
                                                  "amount": 1000000,
                                                  "amountBtc": "0.01000000",
                                                  "txCount": 1,
                                                  "txids": [
                                                    "4d3f1f0d2a8b7c6e5f4a3b2c1d0e9f876543210abcdef1234567890abcdef12"
                                                  ]
                                                }
                                              ]
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Wallet inválida o parámetro depth incorrecto",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiErrorDTO.class),
                            examples = @ExampleObject(
                                    name = "BadRequest",
                                    value = """
                                            {
                                              "error": "BAD_REQUEST",
                                              "message": "La wallet indicada no tiene un formato BTC valido.",
                                              "timestamp": "2026-04-23T10:15:30Z"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "502",
                    description = "Error al consultar la API externa de Blockstream",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiErrorDTO.class),
                            examples = @ExampleObject(
                                    name = "BlockstreamError",
                                    value = """
                                            {
                                              "error": "BLOCKSTREAM_API_ERROR",
                                              "message": "No se pudo consultar Blockstream para la wallet bc1qxy2kgdygjrsqtzq2n0yrf2493p83kkfjhx0wlh. Estado HTTP=429",
                                              "timestamp": "2026-04-23T10:15:30Z"
                                            }
                                            """
                            )
                    )
            )
    })
    @GetMapping("/{wallet}")
    public GraphDTO traceWallet(
            @Parameter(
                    description = "Dirección Bitcoin a analizar",
                    required = true,
                    example = "bc1qxy2kgdygjrsqtzq2n0yrf2493p83kkfjhx0wlh"
            )
            @PathVariable String wallet,

            @Parameter(
                    description = "Profundidad máxima del trazado",
                    example = "2"
            )
            @RequestParam(defaultValue = "2") int depth) {

        return traceService.trace(wallet, depth);
    }


    @Operation(
            summary = "Obtiene bloques donde aparece una wallet",
            description = "Devuelve el primer y último bloque confirmado donde aparece la wallet, " +
                    "además del detalle de transacciones confirmadas y no confirmadas."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Resumen de bloques obtenido correctamente",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = WalletBlockSummaryDTO.class),
                            examples = @ExampleObject(
                                    name = "WalletBlocksExample",
                                    value = """
                                            {
                                              "wallet": "bc1qxy2kgdygjrsqtzq2n0yrf2493p83kkfjhx0wlh",
                                              "firstSeenBlock": 840123,
                                              "lastSeenBlock": 845678,
                                              "confirmedTxCount": 2,
                                              "unconfirmedTxCount": 1,
                                              "transactions": [
                                                {
                                                  "txid": "4d3f1f0d2a8b7c6e5f4a3b2c1d0e9f876543210abcdef1234567890abcdef12",
                                                  "confirmed": true,
                                                  "blockHeight": 840123,
                                                  "blockTime": 1713436800,
                                                  "walletOccurrences": 1
                                                },
                                                {
                                                  "txid": "7ab91c0d5f2e3a4b6c8d9e0f11223344556677889900aabbccddeeff00112233",
                                                  "confirmed": true,
                                                  "blockHeight": 845678,
                                                  "blockTime": 1713523200,
                                                  "walletOccurrences": 2
                                                },
                                                {
                                                  "txid": "9f8e7d6c5b4a39281716151413121110ffeeddccbbaa99887766554433221100",
                                                  "confirmed": false,
                                                  "blockHeight": null,
                                                  "blockTime": null,
                                                  "walletOccurrences": 1
                                                }
                                              ]
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "La wallet no tiene un formato válido",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiErrorDTO.class),
                            examples = @ExampleObject(
                                    name = "BadRequest",
                                    value = """
                                            {
                                              "error": "BAD_REQUEST",
                                              "message": "La wallet indicada no tiene un formato BTC valido.",
                                              "timestamp": "2026-04-23T10:15:30Z"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "502",
                    description = "Error al consultar la API externa de Blockstream",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiErrorDTO.class),
                            examples = @ExampleObject(
                                    name = "BlockstreamError",
                                    value = """
                                            {
                                              "error": "BLOCKSTREAM_API_ERROR",
                                              "message": "No se pudo consultar Blockstream para la wallet bc1qxy2kgdygjrsqtzq2n0yrf2493p83kkfjhx0wlh. Estado HTTP=429",
                                              "timestamp": "2026-04-23T10:15:30Z"
                                            }
                                            """
                            )
                    )
            )
    })
    @GetMapping("/{wallet}/blocks")
    public WalletBlockSummaryDTO getWalletBlocks(
            @Parameter(
                    description = "Dirección Bitcoin a analizar",
                    required = true,
                    example = "bc1qxy2kgdygjrsqtzq2n0yrf2493p83kkfjhx0wlh"
            )
            @PathVariable String wallet) {

        if (!JsonParserUtil.isValidBitcoinAddress(wallet)) {
            throw new IllegalArgumentException("La wallet indicada no tiene un formato BTC valido.");
        }

        return traceService.getWalletBlockSummary(wallet);
    }
}