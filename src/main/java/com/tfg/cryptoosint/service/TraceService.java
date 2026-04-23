package com.tfg.cryptoosint.service;

import com.tfg.cryptoosint.crawler.TransactionCrawler;
import com.tfg.cryptoosint.dto.GraphDTO;
import com.tfg.cryptoosint.dto.WalletBlockSummaryDTO;
import com.tfg.cryptoosint.dto.WalletBlockTxDTO;
import com.tfg.cryptoosint.util.JsonParserUtil;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TraceService {

    private final TransactionCrawler crawler;
    private final BlockClient blockClient;

    public TraceService(TransactionCrawler crawler, BlockClient blockClient) {
        this.crawler = crawler;
        this.blockClient = blockClient;
    }

    public GraphDTO trace(String wallet, int depth) {

        return crawler.traceWallet(wallet, depth);
    }

    public WalletBlockSummaryDTO getWalletBlockSummary(String wallet) {

        String response = blockClient.getAddress(wallet);
        List<WalletBlockTxDTO> transactions = JsonParserUtil.extractWalletBlockAppearances(response, wallet);

        Integer firstSeenBlock = transactions.stream()
                .filter(WalletBlockTxDTO::isConfirmed)
                .map(WalletBlockTxDTO::getBlockHeight)
                .filter(java.util.Objects::nonNull)
                .min(Integer::compareTo)
                .orElse(null);

        Integer lastSeenBlock = transactions.stream()
                .filter(WalletBlockTxDTO::isConfirmed)
                .map(WalletBlockTxDTO::getBlockHeight)
                .filter(java.util.Objects::nonNull)
                .max(Integer::compareTo)
                .orElse(null);

        int confirmedCount = (int) transactions.stream()
                .filter(WalletBlockTxDTO::isConfirmed)
                .count();

        int unconfirmedCount = transactions.size() - confirmedCount;

        return new WalletBlockSummaryDTO(
                wallet,
                firstSeenBlock,
                lastSeenBlock,
                confirmedCount,
                unconfirmedCount,
                transactions
        );
    }
}