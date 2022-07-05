package com.netcracker.application.controller.form;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SearchForm {
    public static final Double MIN_PRICE = 0d;
    public static final Double MAX_PRICE = 1_000_000_000d;

    private String categoryName;
    private String productName;
    private String makerName;
    private Double minPrice;
    private Double maxPrice;
}
