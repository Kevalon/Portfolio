package com.netcracker.application.service.model.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.Set;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Entity
@Table(name = "orders")
public class Order implements MappableEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private BigInteger id;
    @Column(name = "user_id")
    private BigInteger userId;
    @Column(name = "total_sum")
    private Double totalSum;
    @Column(name = "amount_of_goods", nullable = false)
    private Integer goodsAmount;
    @Column(name = "creation_date")
    private Timestamp creationDate;
    @Column(name = "client_name", nullable = false)
    private String name;
    @Column(name = "client_address", nullable = false)
    private String address;
    @Column(name = "client_phone", nullable = false)
    private String phoneNumber;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "orders_and_product",
            joinColumns = {@JoinColumn(name = "order_id")},
            inverseJoinColumns = {@JoinColumn(name = "product_id")})
    private Set<Product> products;
}
