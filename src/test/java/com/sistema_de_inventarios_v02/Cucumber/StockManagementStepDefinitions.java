package com.sistema_de_inventarios_v02.Cucumber;

import com.sistema_de_inventarios_v02.model.Product;
import com.sistema_de_inventarios_v02.dto.ProductDTO;
import com.sistema_de_inventarios_v02.service.ProductService;
import com.sistema_de_inventarios_v02.repository.ProductRepository;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.cucumber.java.en.Then;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class StockManagementStepDefinitions {

    @Autowired
    private ProductService productService;
    
    @Autowired
    private ProductRepository productRepository;

    private Product currentProduct;
    private ProductDTO currentProductDTO;
    private boolean lowStockFlag;
    private boolean outOfStockFlag;

    @Given("the inventory system is initialized")
    public void the_inventory_system_is_initialized() {
        // Clean up any existing test data
        productRepository.deleteAll();
    }

    @Given("a product {string} with stock {int} and minimum stock {int}")
    public void a_product_with_stock_and_minimum_stock(String productName, Integer stock, Integer minimumStock) {
        currentProduct = new Product();
        currentProduct.setName(productName);
        currentProduct.setCategory("Test Category");
        currentProduct.setPrice(new BigDecimal("99.99"));
        currentProduct.setStock(stock);
        currentProduct.setMinimumStock(minimumStock);
    }

    @Given("a product {string} with null stock and minimum stock {int}")
    public void a_product_with_null_stock_and_minimum_stock(String productName, Integer minimumStock) {
        currentProduct = new Product();
        currentProduct.setName(productName);
        currentProduct.setCategory("Test Category");
        currentProduct.setPrice(new BigDecimal("99.99"));
        currentProduct.setStock(null);
        currentProduct.setMinimumStock(minimumStock);
    }

    @When("I check if the product has low stock")
    public void i_check_if_the_product_has_low_stock() {
        lowStockFlag = currentProduct.isLowStock();
    }

    @When("I check if the product is out of stock")
    public void i_check_if_the_product_is_out_of_stock() {
        outOfStockFlag = currentProduct.isOutOfStock();
    }

    @When("I check the product stock status")
    public void i_check_the_product_stock_status() {
        lowStockFlag = currentProduct.isLowStock();
        outOfStockFlag = currentProduct.isOutOfStock();
    }

    @When("I update the product stock to {int}")
    public void i_update_the_product_stock_to(Integer newStock) {
        // First save the product to get an ID
        currentProduct = productRepository.save(currentProduct);
        
        // Then update the stock using the service
        currentProductDTO = productService.updateProductStock(currentProduct.getId(), newStock);
        
        // Reload the product from database to verify
        currentProduct = productRepository.findById(currentProduct.getId()).orElse(null);
    }

    @Then("the product should be flagged as low stock")
    public void the_product_should_be_flagged_as_low_stock() {
        assertThat(lowStockFlag).isTrue();
    }

    @Then("the product should not be flagged as low stock")
    public void the_product_should_not_be_flagged_as_low_stock() {
        assertThat(lowStockFlag).isFalse();
    }

    @Then("the product should be flagged as out of stock")
    public void the_product_should_be_flagged_as_out_of_stock() {
        assertThat(outOfStockFlag).isTrue();
    }

    @Then("the product stock should be {int}")
    public void the_product_stock_should_be(Integer expectedStock) {
        assertThat(currentProduct.getStock()).isEqualTo(expectedStock);
        assertThat(currentProductDTO.getStock()).isEqualTo(expectedStock);
    }

    @Then("the low stock flag should be {word}")
    public void the_low_stock_flag_should_be(String expectedFlag) {
        boolean expected = Boolean.parseBoolean(expectedFlag);
        assertThat(lowStockFlag).isEqualTo(expected);
    }

    @Then("the out of stock flag should be {word}")
    public void the_out_of_stock_flag_should_be(String expectedFlag) {
        boolean expected = Boolean.parseBoolean(expectedFlag);
        assertThat(outOfStockFlag).isEqualTo(expected);
    }
}