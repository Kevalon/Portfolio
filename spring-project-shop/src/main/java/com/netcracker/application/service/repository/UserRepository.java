package com.netcracker.application.service.repository;

import com.netcracker.application.service.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.math.BigInteger;

@Repository
public interface UserRepository extends JpaRepository<User, BigInteger> {
    User findByUsername(String username);
}
