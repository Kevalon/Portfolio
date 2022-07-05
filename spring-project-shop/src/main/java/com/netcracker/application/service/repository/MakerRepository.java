package com.netcracker.application.service.repository;

import com.netcracker.application.service.model.entity.Maker;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.math.BigInteger;

@Repository
public interface MakerRepository extends JpaRepository<Maker, BigInteger> {
    Maker findByName(String name);
}
