package com.tfg.cryptoosint.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Resumen de bloques donde aparece una wallet")
public class WalletBlockSummaryDTO {

    @Schema(description = "Wallet analizada", example = "bc1qxy2kgdygjrsqtzq2n0yrf2493p83kkfjhx0wlh")
    private String wallet;

    @Schema(description = "Primer bloque confirmado donde aparece", example = "840123", nullable = true)
    private Integer firstSeenBlock;

    @Schema(description = "Último bloque confirmado donde aparece", example = "845678", nullable = true)
    private Integer lastSeenBlock;

    @Schema(description = "Número de transacciones confirmadas", example = "12")
    private int confirmedTxCount;

    @Schema(description = "Número de transacciones no confirmadas", example = "1")
    private int unconfirmedTxCount;

    @Schema(description = "Listado detallado de transacciones donde aparece la wallet")
    private List<WalletBlockTxDTO> transactions;

    public WalletBlockSummaryDTO() {
    }

    public WalletBlockSummaryDTO(String wallet,
                                 Integer firstSeenBlock,
                                 Integer lastSeenBlock,
                                 int confirmedTxCount,
                                 int unconfirmedTxCount,
                                 List<WalletBlockTxDTO> transactions) {
        this.wallet = wallet;
        this.firstSeenBlock = firstSeenBlock;
        this.lastSeenBlock = lastSeenBlock;
        this.confirmedTxCount = confirmedTxCount;
        this.unconfirmedTxCount = unconfirmedTxCount;
        this.transactions = transactions;
    }

    public String getWallet() {
        return wallet;
    }

    public void setWallet(String wallet) {
        this.wallet = wallet;
    }

    public Integer getFirstSeenBlock() {
        return firstSeenBlock;
    }

    public void setFirstSeenBlock(Integer firstSeenBlock) {
        this.firstSeenBlock = firstSeenBlock;
    }

    public Integer getLastSeenBlock() {
        return lastSeenBlock;
    }

    public void setLastSeenBlock(Integer lastSeenBlock) {
        this.lastSeenBlock = lastSeenBlock;
    }

    public int getConfirmedTxCount() {
        return confirmedTxCount;
    }

    public void setConfirmedTxCount(int confirmedTxCount) {
        this.confirmedTxCount = confirmedTxCount;
    }

    public int getUnconfirmedTxCount() {
        return unconfirmedTxCount;
    }

    public void setUnconfirmedTxCount(int unconfirmedTxCount) {
        this.unconfirmedTxCount = unconfirmedTxCount;
    }

    public List<WalletBlockTxDTO> getTransactions() {
        return transactions;
    }

    public void setTransactions(List<WalletBlockTxDTO> transactions) {
        this.transactions = transactions;
    }
}

