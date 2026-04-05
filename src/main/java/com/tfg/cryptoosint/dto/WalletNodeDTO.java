package com.tfg.cryptoosint.dto;

public class WalletNodeDTO {

    private String wallet;
    private long amountSatoshis;
    private String amountBtc;

    public WalletNodeDTO() {
    }

    public WalletNodeDTO(String wallet, long amountSatoshis, String amountBtc) {
        this.wallet = wallet;
        this.amountSatoshis = amountSatoshis;
        this.amountBtc = amountBtc;
    }

    public String getWallet() {
        return wallet;
    }

    public void setWallet(String wallet) {
        this.wallet = wallet;
    }

    public long getAmountSatoshis() {
        return amountSatoshis;
    }

    public void setAmountSatoshis(long amountSatoshis) {
        this.amountSatoshis = amountSatoshis;
    }

    public String getAmountBtc() {
        return amountBtc;
    }

    public void setAmountBtc(String amountBtc) {
        this.amountBtc = amountBtc;
    }
}

