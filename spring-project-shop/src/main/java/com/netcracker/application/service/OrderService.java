package com.netcracker.application.service;

import com.netcracker.application.controller.form.OrderDisplayForm;
import com.netcracker.application.controller.form.ProfileEditForm;
import com.netcracker.application.service.model.entity.Order;
import com.netcracker.application.service.model.entity.Product;
import com.netcracker.application.service.model.entity.User;
import com.netcracker.application.service.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class OrderService {
    private final Map<BigInteger, Order> orders = new HashMap<>();
    private final OrderRepository orderRepository;
    private final CartService cartService;
    private final ProductService productService;
    private final UserServiceImpl userService;

    @Autowired
    public OrderService(
            OrderRepository orderRepository,
            CartService cartService,
            ProductService productService,
            UserServiceImpl userService) {
        this.orderRepository = orderRepository;
        this.cartService = cartService;
        this.productService = productService;
        this.userService = userService;
    }

    public boolean isValid(ProfileEditForm profileEditForm) throws IllegalAccessException {
        for (Field f : ProfileEditForm.class.getDeclaredFields()) {
            f.setAccessible(true);
            if (Objects.isNull(f.get(profileEditForm)) || f.get(profileEditForm).equals(""))
                return false;
            f.setAccessible(false);
        }
        return true;
    }

    public void formOrder(User user, ProfileEditForm profileEditForm) {
        Order order = new Order();
        order.setUserId(user.getId());
        order.setTotalSum(cartService.getTotalCost(user.getCart()));
        order.setGoodsAmount(user.getCart().size());
        order.setCreationDate(new Timestamp(new Date().getTime()));
        order.setAddress(profileEditForm.getAddress());
        order.setPhoneNumber(profileEditForm.getPhoneNumber());
        order.setName(profileEditForm.getFirstName() + " " + profileEditForm.getLastName());
        order.setProducts(new HashSet<>(user.getCart()));
        add(order);

        user.setCart(new ArrayList<>());
    }

    public OrderDisplayForm convertOrderToOrderDisplayForm(Order order) {
        OrderDisplayForm orderDisplayForm = new OrderDisplayForm();
        orderDisplayForm.setOrderId(order.getId());
        orderDisplayForm.setCustomerAddress(order.getAddress());
        orderDisplayForm.setUsername(userService.getById(order.getUserId()).getUsername());
        orderDisplayForm.setCustomerName(order.getName());
        orderDisplayForm.setUserId(order.getUserId());
        orderDisplayForm.setCustomerPhoneNumber(order.getPhoneNumber());
        orderDisplayForm.setCreationDate(order.getCreationDate());
        orderDisplayForm.setTotalSum(order.getTotalSum());

        return orderDisplayForm;
    }

    public List<OrderDisplayForm> getListOfOrderDisplayForm(List<Order> orders) {
        return orders.stream()
                .map(this::convertOrderToOrderDisplayForm)
                .collect(Collectors.toList());
    }

    private void fill() {
        if (orders.isEmpty()) {
            for (Order order : orderRepository.findAll()) {
                orders.put(order.getId(), order);
            }
        }
    }

    public List<Order> getAll() {
        fill();
        return new ArrayList<>(orders.values());
    }

    public List<Order> getAllOrdersForOneUser(User user) {
        return getAll().stream().filter(o -> o.getUserId().equals(user.getId())).collect(Collectors.toList());
    }

    public List<Product> getProductsForOneOrder(BigInteger orderId) {
        return new ArrayList<>(getById(orderId).getProducts());
    }

    public Order getById(BigInteger id) {
        fill();
        return orders.get(id);
    }

    public void add(Order order) {
        orderRepository.save(order);

        Map<Product, Long> productsToRemove = order.getProducts()
                .stream()
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
        for (Map.Entry<Product, Long> entry : productsToRemove.entrySet()) {
            BigInteger id = entry.getKey().getId();
            for (long i = 0; i < entry.getValue(); i++) {
                productService.buyOne(id);
            }
        }
        orders.clear();
    }

    public void delete(BigInteger id) {
        orderRepository.delete(orders.get(id));
        orders.remove(id);
    }
}
