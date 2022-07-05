package com.netcracker.application.service.repository;

import com.netcracker.application.service.model.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.math.BigInteger;
import java.sql.Timestamp;

@Repository
public interface OrderRepository extends JpaRepository<Order, BigInteger> {
    Order findByCreationDateAndUserId(Timestamp creationDate, BigInteger userId);
}
