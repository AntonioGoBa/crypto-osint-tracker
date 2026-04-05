package com.tfg.cryptoosint.dto;

import java.util.ArrayList;
import java.util.List;

public class TransactionDTO {

    private String from;
    private String to;
    private long amount;
    private String amountBtc;
    private int txCount;
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