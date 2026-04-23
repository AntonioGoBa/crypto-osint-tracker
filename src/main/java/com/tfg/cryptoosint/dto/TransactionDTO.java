package com.tfg.cryptoosint.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.ArrayList;
import java.util.List;

@Schema(description = "Arista del grafo que representa transferencias agregadas entre dos wallets")
public class TransactionDTO {

    @Schema(description = "Wallet origen", example = "bc1qsourceexample0000000000000000000000000")
    private String from;

    @Schema(description = "Wallet destino", example = "bc1qtargetexample0000000000000000000000000")
    private String to;

    @Schema(description = "Importe agregado en satoshis", example = "250000")
    private long amount;

    @Schema(description = "Importe agregado en BTC", example = "0.00250000")
    private String amountBtc;

    @Schema(description = "Número de transacciones agrupadas entre origen y destino", example = "3")
    private int txCount;

    @Schema(description = "Listado de txids agregados para esta relación")
    private List<String> txids;

    public TransactionDTO() {
        this.txids = new ArrayList<>();
    }

    public TransactionDTO(String from, String to, long amount) {
        this.from = from;
        this.to = to;
        this.amount = amount;
    }

    public TransactionDTO(String from, String to, long amount, String amountBtc) {
        this.from = from;
        this.to = to;
        this.amount = amount;
        this.amountBtc = amountBtc;
        this.txids = new ArrayList<>();
    }

    public TransactionDTO(String from,
                          String to,
                          long amount,
                          String amountBtc,
                          int txCount,
                          List<String> txids) {
        this.from = from;
        this.to = to;
        this.amount = amount;
        this.amountBtc = amountBtc;
        this.txCount = txCount;
        this.txids = txids == null ? new ArrayList<>() : new ArrayList<>(txids);
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public long getAmount() {
        return amount;
    }

    public void setAmount(long amount) {
        this.amount = amount;
    }

    public String getAmountBtc() {
        return amountBtc;
    }

    public void setAmountBtc(String amountBtc) {
        this.amountBtc = amountBtc;
    }

    public int getTxCount() {
        return txCount;
    }

    public void setTxCount(int txCount) {
        this.txCount = txCount;
    }

    public List<String> getTxids() {
        return txids;
    }

    public void setTxids(List<String> txids) {
        this.txids = txids == null ? new ArrayList<>() : new ArrayList<>(txids);
    }

}