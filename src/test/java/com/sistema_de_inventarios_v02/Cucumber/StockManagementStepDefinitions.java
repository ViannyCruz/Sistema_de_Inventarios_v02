package com.sistema_de_inventarios_v02.Cucumber;

import com.sistema_de_inventarios_v02.model.Product;
import com.sistema_de_inventarios_v02.dto.*;
import com.sistema_de_inventarios_v02.service.ProductService;
import com.sistema_de_inventarios_v02.repository.ProductRepository;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.cucumber.java.en.Then;
import io.cucumber.java.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class StockManagementStepDefinitions {

    @Autowired
    private ProductService productService;

    @Autowired
    private ProductRepository productRepository;

    private Product currentProduct;
    private ProductResponseDTO currentProductResponseDTO;
    private StockUpdateDTO stockUpdateDTO;
    private boolean lowStockFlag;
    private boolean outOfStockFlag;

    @Before
    public void setUpSecurityContext() {
        Authentication auth = new TestingAuthenticationToken(
                "testuser",
                "password",
                List.of(
                        new SimpleGrantedAuthority("ROLE_USER"),
                        new SimpleGrantedAuthority("ROLE_ADMIN")
                )
        );
        auth.setAuthenticated(true);

        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        securityContext.setAuthentication(auth);
        SecurityContextHolder.setContext(securityContext);
    }

    @Given("the inventory system is initialized")
    public void the_inventory_system_is_initialized() {
        productRepository.deleteAll();
    }

    @Given("a product {string} with stock {int} and minimum stock {int}")
    public void a_product_with_stock_and_minimum_stock(String productName, Integer stock, Integer minimumStock) {
        CreateProductDTO createProductDTO = new CreateProductDTO(
                productName,
                "Test product description",
                "Test Category",
                new BigDecimal("99.99"),
                stock,
                minimumStock
        );

        currentProductResponseDTO = productService.createProduct(createProductDTO);
        currentProduct = productRepository.findById(currentProductResponseDTO.getId()).orElse(null);
    }

    @When("I check if the product has low stock")
    public void i_check_if_the_product_has_low_stock() {
        lowStockFlag = currentProduct.isLowStock();

        if (currentProductResponseDTO != null) {
            assertThat(currentProductResponseDTO.isLowStock()).isEqualTo(lowStockFlag);
        }
    }

    @When("I check if the product is out of stock")
    public void i_check_if_the_product_is_out_of_stock() {
        outOfStockFlag = currentProduct.isOutOfStock();

        if (currentProductResponseDTO != null) {
            assertThat(currentProductResponseDTO.isOutOfStock()).isEqualTo(outOfStockFlag);
        }
    }

    @When("I check the product stock status")
    public void i_check_the_product_stock_status() {
        lowStockFlag = currentProduct.isLowStock();
        outOfStockFlag = currentProduct.isOutOfStock();

        if (currentProductResponseDTO != null) {
            assertThat(currentProductResponseDTO.isLowStock()).isEqualTo(lowStockFlag);
            assertThat(currentProductResponseDTO.isOutOfStock()).isEqualTo(outOfStockFlag);

            String expectedStatus;
            if (outOfStockFlag) {
                expectedStatus = "OUT_OF_STOCK";
            } else if (lowStockFlag) {
                expectedStatus = "LOW_STOCK";
            } else {
                expectedStatus = "IN_STOCK";
            }
            assertThat(currentProductResponseDTO.getStockStatus()).isEqualTo(expectedStatus);
        }
    }

    @When("I update the product stock to {int}")
    public void i_update_the_product_stock_to(Integer newStock) {
        stockUpdateDTO = new StockUpdateDTO(newStock, "Stock update from Cucumber test");
        currentProductResponseDTO = productService.updateProductStock(currentProduct.getId(), stockUpdateDTO);
        currentProduct = productRepository.findById(currentProduct.getId()).orElse(null);
    }

    @When("I update the product stock to {int} with reason {string}")
    public void i_update_the_product_stock_to_with_reason(Integer newStock, String reason) {
        stockUpdateDTO = new StockUpdateDTO(newStock, reason);
        currentProductResponseDTO = productService.updateProductStock(currentProduct.getId(), stockUpdateDTO);
        currentProduct = productRepository.findById(currentProduct.getId()).orElse(null);
    }

    @Then("the product should not be flagged as low stock")
    public void the_product_should_not_be_flagged_as_low_stock() {
        assertThat(lowStockFlag).isFalse();
        assertThat(currentProductResponseDTO.isLowStock()).isFalse();
    }

    @Then("the product should be flagged as out of stock")
    public void the_product_should_be_flagged_as_out_of_stock() {
        assertThat(outOfStockFlag).isTrue();
        assertThat(currentProductResponseDTO.isOutOfStock()).isTrue();
        assertThat(currentProductResponseDTO.getStockStatus()).isEqualTo("OUT_OF_STOCK");
    }

    @Then("the product stock should be {int}")
    public void the_product_stock_should_be(Integer expectedStock) {
        assertThat(currentProduct.getStock()).isEqualTo(expectedStock);
        assertThat(currentProductResponseDTO.getStock()).isEqualTo(expectedStock);
    }

    @Then("the low stock flag should be {word}")
    public void the_low_stock_flag_should_be(String expectedFlag) {
        boolean expected = Boolean.parseBoolean(expectedFlag);
        assertThat(lowStockFlag).isEqualTo(expected);
        assertThat(currentProductResponseDTO.isLowStock()).isEqualTo(expected);
    }

    @Then("the out of stock flag should be {word}")
    public void the_out_of_stock_flag_should_be(String expectedFlag) {
        boolean expected = Boolean.parseBoolean(expectedFlag);
        assertThat(outOfStockFlag).isEqualTo(expected);
        assertThat(currentProductResponseDTO.isOutOfStock()).isEqualTo(expected);
    }

    @Then("the stock status should be {string}")
    public void the_stock_status_should_be(String expectedStatus) {
        assertThat(currentProductResponseDTO.getStockStatus()).isEqualTo(expectedStatus);
    }

    @Then("the stock update reason should be {string}")
    public void the_stock_update_reason_should_be(String expectedReason) {
        assertThat(stockUpdateDTO.getReason()).isEqualTo(expectedReason);
    }
}