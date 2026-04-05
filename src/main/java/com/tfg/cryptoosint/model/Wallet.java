package com.tfg.cryptoosint.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;

@Getter
@Setter
@Node
public class Wallet {

    @Id
    private String address;

    private Double balance;
}