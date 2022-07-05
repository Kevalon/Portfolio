package com.netcracker.application.controller.form;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigInteger;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductAddForm {
    private BigInteger productId;
    private String name;
    private String description;
    private Integer amountInShop;
    private String makerName;
    private Double price;
    private Double discount;
    private String categoryName;
}
