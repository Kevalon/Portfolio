package com.netcracker.application.controller.form;

import com.netcracker.application.service.model.entity.Category;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigInteger;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductDisplayForm {
    private BigInteger productId;
    private String name;
    private Integer amount;
    private String description;
    private Double priceWithDiscount;
    private String makerName;
    private List<Category> categories;
}
