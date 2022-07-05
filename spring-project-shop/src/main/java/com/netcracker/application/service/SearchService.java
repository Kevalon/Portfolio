package com.netcracker.application.service;

import com.netcracker.application.controller.form.SearchForm;
import com.netcracker.application.service.model.entity.Product;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class SearchService {
    private final ProductService productService;
    private final MakerService makerService;

    @Autowired
    public SearchService(ProductService productService, MakerService makerService) {
        this.productService = productService;
        this.makerService = makerService;
    }

    public List<Product> getResult(SearchForm searchForm) {
        //Stream<Product> resultStream = productService.getAll().stream();
        List<Product> list = productService.getAll();
        Stream<Product> resultStream = list.stream();

        if (!searchForm.getCategoryName().equals("")) {
            resultStream = resultStream
                    .filter(p -> p.getCategories()
                            .stream().anyMatch(c -> c.getName().contains(searchForm.getCategoryName())));
        }
        if (!searchForm.getProductName().equals("")) {
            resultStream = resultStream.filter(p -> p.getName().contains(searchForm.getProductName()));
        }
        if (!searchForm.getMakerName().equals("")) {
            resultStream =
                    resultStream.filter(p -> makerService.getById(p.getMakerId()).getName().contains(searchForm.getMakerName()));
        }
        resultStream = resultStream
                .filter(p -> p.getPrice() >= searchForm.getMinPrice() && p.getPrice() <= searchForm.getMaxPrice());

        return resultStream.collect(Collectors.toList());
    }
}
