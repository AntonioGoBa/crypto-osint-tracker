package com.tfg.cryptoosint.dto;

import java.util.List;

public class GraphDTO {

    private String initialWallet;
    private long initialAmountSatoshis;
    private String initialAmountBtc;
    private List<WalletNodeDTO> nodes;
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