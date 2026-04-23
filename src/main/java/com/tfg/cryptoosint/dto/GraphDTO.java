package com.tfg.cryptoosint.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Resultado del trazado de una wallet en forma de grafo")
public class GraphDTO {

    @Schema(description = "Wallet inicial analizada", example = "bc1qxy2kgdygjrsqtzq2n0yrf2493p83kkfjhx0wlh")
    private String initialWallet;

    @Schema(description = "Balance o importe asociado en satoshis", example = "1250000")
    private long initialAmountSatoshis;

    @Schema(description = "Balance o importe asociado en BTC", example = "0.01250000")
    private String initialAmountBtc;

    @Schema(description = "Nodos del grafo")
    private List<WalletNodeDTO> nodes;

    @Schema(description = "Aristas del grafo")
    private List<TransactionDTO> edges;

    public GraphDTO() {
    }

    public GraphDTO(String initialWallet,
                    long initialAmountSatoshis,
                    String initialAmountBtc,
                    List<WalletNodeDTO> nodes,
                    List<TransactionDTO> edges) {
        this.initialWallet = initialWallet;
        this.initialAmountSatoshis = initialAmountSatoshis;
        this.initialAmountBtc = initialAmountBtc;
        this.nodes = nodes;
        this.edges = edges;
    }

    public String getInitialWallet() {
        return initialWallet;
    }

    public void setInitialWallet(String initialWallet) {
        this.initialWallet = initialWallet;
    }

    public long getInitialAmountSatoshis() {
        return initialAmountSatoshis;
    }

    public void setInitialAmountSatoshis(long initialAmountSatoshis) {
        this.initialAmountSatoshis = initialAmountSatoshis;
    }

    public String getInitialAmountBtc() {
        return initialAmountBtc;
    }

    public void setInitialAmountBtc(String initialAmountBtc) {
        this.initialAmountBtc = initialAmountBtc;
    }

    public List<WalletNodeDTO> getNodes() {
        return nodes;
    }

    public void setNodes(List<WalletNodeDTO> nodes) {
        this.nodes = nodes;
    }

    public List<TransactionDTO> getEdges() {
        return edges;
    }

    public void setEdges(List<TransactionDTO> edges) {
        this.edges = edges;
    }

}