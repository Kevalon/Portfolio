package com.netcracker.application.service;

import com.netcracker.application.service.model.entity.Product;
import com.netcracker.application.service.model.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class CartService {
    private final UserServiceImpl userService;
    private final ProductService productService;

    @Autowired
    public CartService(UserServiceImpl userService, ProductService productService) {
        this.userService = userService;
        this.productService = productService;
    }

    public void addToCart(Product product) {
        userService.getCurrentUser().getCart().add(product);
    }

    public void deleteFromCart(BigInteger productId, User user) {
        user.getCart().remove(productService.getById(productId));
    }

    public double getTotalCost(List<Product> cart) {
        return cart
                .stream()
                .map(p -> p.getPrice() * (1.0 - Optional.ofNullable(p.getDiscount()).orElse(0.0)))
                .reduce(0.0, Double::sum);
    }

    public boolean areProductsStillAvailable(User user) {
        List<Product> cart = user.getCart();
        Map<Product, Long> frequency = user.getCart()
                .stream()
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
        for (Map.Entry<Product, Long> entry : frequency.entrySet()) {
            Product product = productService.getById(entry.getKey().getId());
            if (Objects.isNull(product) || product.getAmountInShop() < entry.getValue()) {
                return false;
            }
        }
        return true;
    }
}
