package com.tfg.cryptoosint.repository;

import com.tfg.cryptoosint.model.Wallet;
import org.springframework.data.neo4j.repository.Neo4jRepository;

public interface WalletRepository extends Neo4jRepository<Wallet, String> {
}