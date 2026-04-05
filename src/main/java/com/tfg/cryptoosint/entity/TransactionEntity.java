package com.tfg.cryptoosint.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class TransactionEntity {

    @Id
    @GeneratedValue
    private Long id;

    private String fromAddress;
    private String toAddress;
    private Long amount;

    public TransactionEntity() {}

    public TransactionEntity(String from, String to, Long amount) {
        this.fromAddress = from;
        this.toAddress = to;
        this.amount = amount;
    }

    // getters/setters
}