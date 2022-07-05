package com.netcracker.application.service.repository;

import com.netcracker.application.service.model.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.math.BigInteger;

@Repository
public interface CategoryRepository extends JpaRepository<Category, BigInteger> {
    Category findByName(String name);
}
