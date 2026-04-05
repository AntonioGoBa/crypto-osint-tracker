package com.tfg.cryptoosint.model;

import org.springframework.data.neo4j.core.schema.*;

@RelationshipProperties
public class Transfer {

    @Id
    @GeneratedValue
    private Long id;

    private String txHash;

    private double amount;

    @TargetNode
    private Wallet to;

}