package com.tfg.cryptoosint.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Detalle de una transacción en la que aparece la wallet analizada")
public class WalletBlockTxDTO {

    @Schema(description = "Identificador de la transacción", example = "4d3f1f0d2a8b7c6e5f4a3b2c1d0e9f876543210abcdef1234567890abcdef12")
    private String txid;

    @Schema(description = "Indica si la transacción está confirmada", example = "true")
    private boolean confirmed;

    @Schema(description = "Altura del bloque en el que se confirmó la transacción", example = "840123", nullable = true)
    private Integer blockHeight;

    @Schema(description = "Marca temporal unix del bloque", example = "1713436800", nullable = true)
    private Long blockTime;

    @Schema(description = "Número de veces que la wallet aparece en la transacción", example = "2")
    private int walletOccurrences;

    public WalletBlockTxDTO() {
    }

    public WalletBlockTxDTO(String txid,
                            boolean confirmed,
                            Integer blockHeight,
                            Long blockTime,
                            int walletOccurrences) {
        this.txid = txid;
        this.confirmed = confirmed;
        this.blockHeight = blockHeight;
        this.blockTime = blockTime;
        this.walletOccurrences = walletOccurrences;
    }

    public String getTxid() {
        return txid;
    }

    public void setTxid(String txid) {
        this.txid = txid;
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    public void setConfirmed(boolean confirmed) {
        this.confirmed = confirmed;
    }

    public Integer getBlockHeight() {
        return blockHeight;
    }

    public void setBlockHeight(Integer blockHeight) {
        this.blockHeight = blockHeight;
    }

    public Long getBlockTime() {
        return blockTime;
    }

    public void setBlockTime(Long blockTime) {
        this.blockTime = blockTime;
    }

    public int getWalletOccurrences() {
        return walletOccurrences;
    }

    public void setWalletOccurrences(int walletOccurrences) {
        this.walletOccurrences = walletOccurrences;
    }
}

