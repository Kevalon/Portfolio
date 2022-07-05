package com.netcracker.application.controller;

import com.netcracker.application.controller.form.SearchForm;
import com.netcracker.application.service.ProductService;
import com.netcracker.application.service.SearchService;
import com.netcracker.application.service.model.entity.Product;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/catalogue/search")
public class SearchProductController {
    private final SearchService searchService;
    private final ProductService productService;

    @Autowired
    public SearchProductController(SearchService searchService, ProductService productService) {
        this.searchService = searchService;
        this.productService = productService;
    }

    @GetMapping
    public String beforeSearch(Model model) {
        SearchForm searchForm = new SearchForm();
        searchForm.setMinPrice(SearchForm.MIN_PRICE);
        searchForm.setMaxPrice(SearchForm.MAX_PRICE);
        model.addAttribute("searchForm", searchForm);
        return "search/before";
    }

    @PostMapping
    public String afterSearch(@ModelAttribute("searchForm") SearchForm searchForm, Model model) {
        List<Product> resultOfSearch = searchService.getResult(searchForm);
        if (resultOfSearch.size() == 0) {
            model.addAttribute("nothing", true);
        } else {
            model.addAttribute("nothing", false);
            model.addAttribute("products", productService.getListOfProductDisplayForm(resultOfSearch));
            return "search/after";
        }
        return "search/after";
    }
}
