package com.netcracker.application.controller;

import com.netcracker.application.controller.form.ProductAddForm;
import com.netcracker.application.controller.form.ProductDisplayForm;
import com.netcracker.application.service.CartService;
import com.netcracker.application.service.ProductService;
import com.netcracker.application.service.UserServiceImpl;
import com.netcracker.application.service.model.entity.Product;
import com.netcracker.application.service.model.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.math.BigInteger;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/catalogue")
public class CatalogueController {
    private final ProductService productService;
    private final CartService cartService;
    private final UserServiceImpl userService;

    @Autowired
    public CatalogueController(ProductService productService, CartService cartService, UserServiceImpl userService) {
        this.productService = productService;
        this.cartService = cartService;
        this.userService = userService;
    }

    @GetMapping
    public String products(ModelMap model) {
        List<ProductDisplayForm> productDisplayForms =
                productService.getListOfProductDisplayForm(productService.getAll());
        model.addAttribute("products", productDisplayForms);
        return "catalogue/list";
    }

    @GetMapping("/{id}")
    public String getProduct(@PathVariable BigInteger id, ModelMap model) {
        Product product = productService.getById(id);
        model.addAttribute(
                "products",
                productService.getListOfProductDisplayForm(new ArrayList<Product>() {{add(product);}}));
        model.addAttribute("productId", id);
        model.addAttribute("title", product.getName());
        return "catalogue/one";
    }

    @PostMapping("/{productId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CUSTOMER')")
    public String addToCart(@PathVariable BigInteger productId, ModelMap model) {
        User curUser = userService.getCurrentUser();
        Product addedProduct = productService.getById(productId);

        if (productService.getById(productId).getAmountInShop()
                - curUser.getSingleProductAmount(addedProduct) == 0) {
            model.addAttribute("error", true);
            return getProduct(productId, model);
        }

        cartService.addToCart(addedProduct);
        model.addAttribute("success", true);
        return getProduct(productId, model);
    }

    @GetMapping("/add")
    @PreAuthorize("hasRole('ADMIN')")
    public String addProduct(ModelMap model) {
        ProductAddForm form = new ProductAddForm();
        form.setAmountInShop(0);
        form.setPrice(0.0);
        model.addAttribute("product", form);
        return "catalogue/add";
    }

    @PostMapping("/add")
    @PreAuthorize("hasRole('ADMIN')")
    public String addProduct(ProductAddForm form, ModelMap model) {
        try {
            productService.addProductFromForm(form);
        } catch (SQLException exception) {
            model.addAttribute("error", true);
            model.addAttribute("errorMessage", exception.getMessage());
            model.addAttribute("product", form);
            return "catalogue/add";
        }

        return "redirect:/catalogue";
    }

    @GetMapping("/{id}/edit")
    @PreAuthorize("hasRole('ADMIN')")
    public String edit(@PathVariable BigInteger id, ModelMap modelMap) {
        Product product = productService.getById(id);
        modelMap.addAttribute("product", productService.getAddForm(product));
        return "catalogue/add";
    }

    @GetMapping("/{id}/delete")
    @PreAuthorize("hasRole('ADMIN')")
    public String delete(@PathVariable BigInteger id) {
        productService.deleteProduct(id);
        return "redirect:/catalogue";
    }
}
