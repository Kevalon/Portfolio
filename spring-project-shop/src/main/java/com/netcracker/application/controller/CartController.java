package com.netcracker.application.controller;

import com.netcracker.application.service.CartService;
import com.netcracker.application.service.ProductService;
import com.netcracker.application.service.UserServiceImpl;
import com.netcracker.application.service.model.entity.Product;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.math.BigInteger;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.List;

@Controller
@RequestMapping("/cart")
@PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN')")
public class CartController {
    private final CartService cartService;
    private final UserServiceImpl userService;
    private final ProductService productService;

    @Autowired
    public CartController(CartService cartService, UserServiceImpl userService, ProductService productService) {
        this.cartService = cartService;
        this.userService = userService;
        this.productService = productService;
    }

    @GetMapping
    public String displayCart(Model model) {
        List<Product> cart = userService.getCurrentUser().getCart();
        if (cart.size() < 1) {
            model.addAttribute("nothing", true);
        } else {
            model.addAttribute("nothing", false);
            model.addAttribute("products", productService.getListOfProductDisplayForm(cart));
            model.addAttribute(
                    "amountOfProducts",
                    "Amount of Products in the cart: " + cart.size());
            DecimalFormat formatter = new DecimalFormat("#.##");
            formatter.setRoundingMode(RoundingMode.DOWN);
            model.addAttribute(
                    "totalSum",
                    "Subtotal: " + formatter.format(cartService.getTotalCost(cart)));
        }
        return "catalogue/cart";
    }

    @GetMapping("/delete/{productId}")
    public String deleteFromCart(@PathVariable BigInteger productId) {
        cartService.deleteFromCart(productId, userService.getCurrentUser());
        return "redirect:/cart";
    }
}
