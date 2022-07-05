package com.netcracker.application.service;

import com.netcracker.application.controller.form.ProductAddForm;
import com.netcracker.application.controller.form.ProductDisplayForm;
import com.netcracker.application.service.model.entity.Category;
import com.netcracker.application.service.model.entity.Maker;
import com.netcracker.application.service.model.entity.Product;
import com.netcracker.application.service.repository.MakerRepository;
import com.netcracker.application.service.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigInteger;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class ProductService {
    private final Map<BigInteger, Product> products = new HashMap<>();
    private final ProductRepository productRepository;
    private final CategoryService categoryService;
    private final MakerRepository makerRepository;
    private final MakerService makerService;

    @Autowired
    public ProductService(
            ProductRepository productRepository,
            CategoryService categoryService,
            MakerRepository makerRepository,
            MakerService makerService) {
        this.productRepository = productRepository;
        this.categoryService = categoryService;
        this.makerRepository = makerRepository;
        this.makerService = makerService;
    }

    private void fill() {
        if (products.isEmpty()) {
            for (Product product : productRepository.findAll()) {
                if (!product.getIsDeleted())
                    products.put(product.getId(), product);
            }
        }
    }

    public List<Product> getAll() {
        fill();
        return new ArrayList<>(products.values());
    }

    public Product getById(BigInteger id) {
        fill();
        return products.get(id);
    }

    public void addProduct(Product product) {
        if (Objects.isNull(product.getId())) {
            Maker maker = makerService.getById(product.getMakerId());
            maker.setProductsAmount(maker.getProductsAmount() + 1);
            makerService.update(maker);

            Category category = categoryService.getById(product.getCategories().get(0).getId());
            category.setProductsAmount(category.getProductsAmount() + 1);
            categoryService.update(category);
        }

        productRepository.save(product);
        products.clear();
    }

    public void addProductFromForm(ProductAddForm form) throws SQLException {
        if (form.getName().equals("")) {
            throw new SQLException("Name must be presented");
        }
        if (form.getPrice() <= 0.0) {
            throw new SQLException("Price can not be negative");
        }
        if (form.getCategoryName().equals("")) {
            throw new SQLException("Category must be specified");
        }
        if (form.getMakerName().equals("")) {
            throw new SQLException("Maker must be specified");
        }
        if (form.getAmountInShop() < 0) {
            throw new SQLException("There can't be less than 0 products in the shop");
        }
        if (!Objects.isNull(form.getDiscount())) {
            if (form.getDiscount() < 0.0 || form.getDiscount() >= 1.0) {
                throw new SQLException("Discount should be between 0.0 and 1.0");
            }
        }
        if (Objects.isNull(categoryService.findByName(form.getCategoryName()))) {
            throw new SQLException("Specified category doesn't exist");
        }
        if (Objects.isNull(makerRepository.findByName(form.getMakerName()))) {
            throw new SQLException("Specified maker doesn't exist");
        }

        Product product;
        if (Objects.isNull(form.getProductId())) {
            product = productRepository.findByName(form.getName());
            if (!Objects.isNull(product)) {
                if (product.getIsDeleted()) {
                    throw new SQLException("Product with such name already exists in the database, " +
                            "but is currently deleted");
                } else throw new SQLException("Product with such name already exists");
            } else product = new Product();
        } else product = getById(form.getProductId());

        product.setIsDeleted(false);
        product.setAmountInShop(form.getAmountInShop());
        product.setCategories(new ArrayList<Category>() {{
            add(categoryService.findByName(form.getCategoryName()));
        }});
        product.setDescription(form.getDescription());
        product.setName(form.getName());
        product.setDiscount(form.getDiscount());
        product.setPrice(form.getPrice());
        product.setMakerId(makerRepository.findByName(form.getMakerName()).getId());
        addProduct(product);
    }

    public void deleteProduct(BigInteger id) {
        Product product = getById(id);

        Maker maker = makerService.getById(product.getMakerId());
        maker.setProductsAmount(maker.getProductsAmount() - 1);
        makerService.update(maker);

        product.getCategories().forEach(c -> c.getProducts().remove(product));
        product.getCategories().forEach(categoryService::deleteOneProduct);

        product.setCategories(null);
        product.setMakerId(null);
        product.setIsDeleted(true);
        productRepository.save(product);
        products.clear();
    }

    public void buyOne(BigInteger productId) {
        Product product = getById(productId);
        product.setAmountInShop(product.getAmountInShop() - 1);
        productRepository.save(product);
        products.clear();
    }

    public ProductDisplayForm convertProductToProductDisplayForm(Product product) {
        ProductDisplayForm productDisplayForm = new ProductDisplayForm();
        productDisplayForm.setName(product.getName());
        productDisplayForm.setProductId(product.getId());
        productDisplayForm.setAmount(product.getAmountInShop());
        productDisplayForm.setCategories(product.getCategories());
        productDisplayForm.setDescription(product.getDescription());
        productDisplayForm.setMakerName(makerService.getById(product.getMakerId()).getName());
        productDisplayForm.setPriceWithDiscount(product.getPrice()
                * (1.0 - Optional.ofNullable(product.getDiscount()).orElse(0.0)));

        return productDisplayForm;
    }

    public ProductAddForm getAddForm(Product product) {
        ProductAddForm form = new ProductAddForm();
        form.setProductId(product.getId());
        form.setName(product.getName());
        form.setDescription(product.getDescription());
        form.setPrice(product.getPrice());
        form.setDiscount(product.getDiscount());
        form.setAmountInShop(product.getAmountInShop());
        form.setMakerName(makerService.getById(product.getMakerId()).getName());
        form.setCategoryName(product.getCategories().get(0).getName());
        return form;
    }

    public List<ProductDisplayForm> getListOfProductDisplayForm(List<Product> products) {
        return products.stream()
                .filter(p -> !p.getIsDeleted())
                .map(this::convertProductToProductDisplayForm)
                .collect(Collectors.toList());
    }

    public boolean categoryIsStillInUse(BigInteger id) {
        return getAll()
                .stream()
                .filter(p -> !p.getIsDeleted())
                .anyMatch(p -> p.getCategories()
                        .stream()
                        .anyMatch(c -> c.getId().equals(id)));
    }
}
