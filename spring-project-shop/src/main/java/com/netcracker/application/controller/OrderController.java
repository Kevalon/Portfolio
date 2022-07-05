package com.netcracker.application.controller;

import com.netcracker.application.controller.form.ProfileEditForm;
import com.netcracker.application.service.CartService;
import com.netcracker.application.service.OrderService;
import com.netcracker.application.service.ProductService;
import com.netcracker.application.service.UserServiceImpl;
import com.netcracker.application.service.model.entity.Order;
import com.netcracker.application.service.model.entity.Product;
import com.netcracker.application.service.model.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.List;

@Controller
@RequestMapping("/order")
@PreAuthorize("hasAnyRole('ADMIN', 'CUSTOMER')")
public class OrderController {
    private final CartService cartService;
    private final UserServiceImpl userService;
    private final OrderService orderService;
    private final ProductService productService;

    @Autowired
    public OrderController(
            CartService cartService,
            UserServiceImpl userService,
            OrderService orderService,
            ProductService productService) {
        this.cartService = cartService;
        this.userService = userService;
        this.orderService = orderService;
        this.productService = productService;
    }

    @GetMapping("/confirm")
    public String confirmOrder(Model model) {
        User user = userService.getCurrentUser();
        if (user.getCart().isEmpty()) {
            return "redirect:/cart";
        }
        if (!cartService.areProductsStillAvailable(user)) {
            model.addAttribute("error", true);
        } else {
            model.addAttribute("error", false);
            model.addAttribute(
                    "profileInfoForm",
                    userService.getProfileEditForm(userService.getCurrentUser()));
            DecimalFormat formatter = new DecimalFormat("#.##");
            formatter.setRoundingMode(RoundingMode.DOWN);
            model.addAttribute(
                    "subTotal",
                    "Subtotal: " + formatter.format(cartService.getTotalCost(user.getCart())));
        }
        return "order/confirm";
    }

    @PostMapping("/confirm")
    public String confirmOrder(@ModelAttribute("profileInfoForm") ProfileEditForm profileEditForm, Model model) {
        boolean check;
        try {
            check = orderService.isValid(profileEditForm);
        } catch (IllegalAccessException e) {
            check = false;
            e.printStackTrace();
        }

        if (!check) {
            model.addAttribute("error", false);
            model.addAttribute("profileDataError", true);
            model.addAttribute("profileInfoForm", profileEditForm);
            model.addAttribute(
                    "subTotal",
                    "Subtotal: " + cartService.getTotalCost(userService.getCurrentUser().getCart()));
            return "/order/confirm";
        }
        orderService.formOrder(userService.getCurrentUser(), profileEditForm);
        return "order/success";
    }

    @GetMapping("/customer")
    public String showCustomerOrders(Model model) {
        List<Order> orders = orderService.getAllOrdersForOneUser(userService.getCurrentUser());
        model.addAttribute("nothing", orders.size() < 1);
        model.addAttribute("orders", orderService.getListOfOrderDisplayForm(orders));
        return "order/customer";
    }

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public String showAllOrders(Model model) {
        model.addAttribute("orders", orderService.getListOfOrderDisplayForm(orderService.getAll()));
        return "order/admin";
    }

    @GetMapping("/{orderId}")
    public String showProductsInOrder(@PathVariable BigInteger orderId, Model model, HttpServletRequest request) {
        String referer = request.getHeader("referer");
        List<Product> products = orderService.getProductsForOneOrder(orderId);
        model.addAttribute("products", productService.getListOfProductDisplayForm(products));
        model.addAttribute("referer", referer);
        return "order/one";
    }
}
