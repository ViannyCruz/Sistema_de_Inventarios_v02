package com.sistema_de_inventarios_v02.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public class ProductDTO {

    private Long id;

    @NotBlank(message = "El nombre del producto es obligatorio")
    @Size(max = 100, message = "El nombre del producto no puede exceder los 100 caracteres")
    private String name;

    @Size(max = 500, message = "La descripción del producto no puede exceder los 500 caracteres")
    private String description;

    @NotBlank(message = "La categoría del producto es obligatoria")
    @Size(max = 70, message = "La categoría del producto no puede exceder los 70 caracteres")
    private String category;

    @NotNull(message = "El precio del producto es obligatorio")
    @DecimalMin(value = "0.0", inclusive = false, message = "El precio debe ser mayor que cero")
    @Digits(integer = 8, fraction = 2, message = "El precio debe ser un número de máximo 8 dígitos y hasta 2 decimales")
    private BigDecimal price;

    @NotNull(message = "El stock del producto es obligatorio")
    @Min(value = 0, message = "El stock no puede ser negativo")
    private Integer stock;

    @Min(value = 0, message = "El stock mínimo no puede ser negativo")
    private Integer minimumStock;

    public ProductDTO() {
    }

    public ProductDTO(Long id, String name, String description, String category, BigDecimal price, Integer stock, Integer minimumStock) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.category = category;
        this.price = price;
        this.stock = stock;
        this.minimumStock = minimumStock;
    }

    public ProductDTO(String name, String description, String category, BigDecimal price, Integer stock, Integer minimumStock) {
        this.name = name;
        this.description = description;
        this.category = category;
        this.price = price;
        this.stock = stock;
        this.minimumStock = minimumStock;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Integer getStock() {
        return stock;
    }

    public void setStock(Integer stock) {
        this.stock = stock;
    }

    public Integer getMinimumStock() {
        return minimumStock;
    }

    public void setMinimumStock(Integer minimumStock) {
        this.minimumStock = minimumStock;
    }

    @Override
    public String toString() {
        return "ProductDTO{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", category='" + category + '\'' +
                ", price=" + price +
                ", stock=" + stock +
                ", minimumStock=" + minimumStock +
                '}';
    }
}