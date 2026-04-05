package com.tfg.cryptoosint.repository;

import com.tfg.cryptoosint.entity.AddressEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AddressRepository extends JpaRepository<AddressEntity, Long> {
    Optional<AddressEntity> findByAddress(String address);
}