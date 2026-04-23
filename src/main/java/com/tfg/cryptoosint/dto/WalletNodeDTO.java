package com.tfg.cryptoosint.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Nodo del grafo que representa una wallet y su balance asociado")
public class WalletNodeDTO {

    @Schema(description = "Dirección de la wallet", example = "bc1qxy2kgdygjrsqtzq2n0yrf2493p83kkfjhx0wlh")
    private String wallet;

    @Schema(description = "Balance o importe asociado en satoshis", example = "1250000")
    private long amountSatoshis;

    @Schema(description = "Balance o importe asociado en BTC", example = "0.01250000")
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

