package com.tfg.cryptoosint.service;

import com.tfg.cryptoosint.crawler.TransactionCrawler;
import com.tfg.cryptoosint.dto.GraphDTO;
import org.springframework.stereotype.Service;

@Service
public class TraceService {

    private final TransactionCrawler crawler;

    public TraceService(TransactionCrawler crawler) {
        this.crawler = crawler;
    }

    public GraphDTO trace(String wallet, int depth) {

        return crawler.traceWallet(wallet, depth);
    }
}