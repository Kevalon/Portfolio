package com.netcracker.application.controller;

import com.netcracker.application.service.*;
import com.netcracker.application.service.model.entity.Category;
import com.netcracker.application.service.model.entity.Maker;
import com.netcracker.application.service.model.entity.Order;
import com.netcracker.application.service.model.entity.User;
import com.netcracker.application.service.model.parser.JsonParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.management.InstanceAlreadyExistsException;
import java.math.BigInteger;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Controller
@PreAuthorize("hasRole('ADMIN')")
@RequestMapping("/management")
public class ShopManagementController {
    private final MakerService makerService;
    private final CategoryService categoryService;
    private final ProductService productService;
    private final UserServiceImpl userService;
    private final OrderService orderService;

    @Autowired
    public ShopManagementController(
            MakerService makerService,
            CategoryService categoryService,
            ProductService productService,
            UserServiceImpl userService,
            OrderService orderService) {
        this.makerService = makerService;
        this.categoryService = categoryService;
        this.productService = productService;
        this.userService = userService;
        this.orderService = orderService;
    }

    @GetMapping
    public String main() {
        return "management/main";
    }

    @GetMapping("/maker")
    public String makers(ModelMap model) {
        List<Maker> makers = makerService.getAll();
        //Map<BigInteger, String> jsonMap = JsonParser.parseToMap(makers);
        model.addAttribute("makers", makers);
        return "management/maker/list";
    }

    @GetMapping("maker/{id}")
    public String getMaker(@PathVariable BigInteger id, ModelMap model) {
        Maker maker = makerService.getById(id);
        model.addAttribute("makers", new ArrayList<Maker>() {{
            add(maker);
        }});
        model.addAttribute("title", maker.getName());
        model.addAttribute("makerId", id);
        return "management/maker/one";
    }

    @GetMapping("maker/add")
    public String addMaker(ModelMap model) {
        model.addAttribute("maker", new Maker());
        return "management/maker/add";
    }

    @PostMapping("maker/add")
    public String addMaker(Maker maker, ModelMap modelMap) {
        try {
            makerService.add(maker);
        } catch (SQLException e) {
            modelMap.addAttribute("maker", maker);
            modelMap.addAttribute("error", true);
            modelMap.addAttribute("errorMessage", e.getMessage());
            return "management/maker/add";
        }

        return "redirect:/management/maker";
    }

    @GetMapping("maker/{id}/edit")
    public String editMaker(@PathVariable BigInteger id, ModelMap modelMap) {
        Maker maker = makerService.getById(id);
        modelMap.addAttribute("maker", maker);
        return "management/maker/add";
    }

    @GetMapping("maker/{id}/delete")
    public String deleteMaker(@PathVariable BigInteger id, ModelMap model) {
        if (productService.getAll().stream().filter(p -> !p.getIsDeleted()).anyMatch(p -> p.getMakerId().equals(id))) {
            model.addAttribute("error", true);
            model.addAttribute("makerId", id);
            return getMaker(id, model);
        }

        makerService.delete(id);
        return "redirect:/management/maker";
    }

    @GetMapping("/category")
    public String categories(ModelMap model) {
        List<Category> categories = categoryService.getAll();
        model.addAttribute("categories", categories);
        return "management/category/list";
    }

    @GetMapping("category/{id}")
    public String getCategory(@PathVariable BigInteger id, ModelMap model) {
        Category category = categoryService.getById(id);
        model.addAttribute("categories", new ArrayList<Category>() {{
            add(category);
        }});
        model.addAttribute("title", category.getName());
        model.addAttribute("categoryId", id);
        return "management/category/one";
    }

    @GetMapping("category/add")
    public String addCategory(ModelMap model) {
        model.addAttribute("category", new Category());
        return "management/category/add";
    }

    @PostMapping("category/add")
    public String addCategory(Category category, Model model) {
        try {
            categoryService.add(category);
        } catch (SQLException e) {
            model.addAttribute("category", category);
            model.addAttribute("error", true);
            model.addAttribute("errorMessage", e.getMessage());
            return "management/category/add";
        }
        return "redirect:/management/category";
    }

    @GetMapping("category/{id}/edit")
    public String editCategory(@PathVariable BigInteger id, ModelMap modelMap) {
        Category category = categoryService.getById(id);
        modelMap.addAttribute("category", category);
        return "management/category/add";
    }

    @GetMapping("category/{id}/delete")
    public String deleteCategory(@PathVariable BigInteger id, ModelMap model) {
        if (productService.categoryIsStillInUse(id)) {
            model.addAttribute("error", true);
            model.addAttribute("categoryId", id);
            return getCategory(id, model);
        }

        categoryService.delete(id);
        return "redirect:/management/category";
    }

    @GetMapping("user")
    public String displayUsers(Model model) {
        model.addAttribute("users", userService.getUserDisplayForms(userService.getAll()));
        return "management/user/list";
    }

    @GetMapping("user/{userId}")
    public String displayUserOrders(@PathVariable BigInteger userId, Model model) {
        User user = userService.getById(userId);
        List<Order> userOrders = orderService.getAllOrdersForOneUser(user);
        model.addAttribute("title", user.getUsername());
        model.addAttribute("orders", orderService.getListOfOrderDisplayForm(userOrders));
        model.addAttribute("nothing", userOrders.size() < 1);
        return "order/customer";
    }
}
