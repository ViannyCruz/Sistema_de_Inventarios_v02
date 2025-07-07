package com.sistema_de_inventarios_v02.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

@Entity
@Table(name = "products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, length = 100)
    @NotBlank(message = "El nombre del producto es obligatorio")
    @Size(max = 100, message = "El nombre del producto no puede exceder los 100 caracteres")
    private String name;

    @Column(name = "description", length = 500)
    @Size(max = 500, message = "La descripción del producto no puede exceder los 500 caracteres")
    private String description;

    @Column(name = "category", nullable = false, length = 70)
    @NotBlank(message = "La categoría del producto es obligatoria")
    @Size(max = 70, message = "La categoría del producto no puede exceder los 70 caracteres")
    private String category;

    @Column(name = "price", nullable = false)
    @NotNull(message = "El precio del producto es obligatorio")
    @DecimalMin(value = "0.0", inclusive = false, message = "El precio debe ser mayor que cero")
    @Digits(integer = 8, fraction = 2, message = "El precio debe ser un número de maximo 8 digitos y  hasta 2 decimales")
    private BigDecimal price;

    @Column(name = "stock", nullable = false)
    @NotNull(message = "El stock del producto es obligatorio")
    @Min(value = 0, message = "El stock no puede ser negativo")
    private Integer stock;

    @Column(name = "minimum_stock", nullable = true)
    @Min(value = 0, message = "El stock mínimo no puede ser negativo")
    private Integer minimumStock;

    public Product() {
    }

    public Product(String name, String description, String category, BigDecimal price, Integer stock, Integer minimumStock) {
        this.name = name;
        this.description = description;
        this.category = category;
        this.price = price;
        this.stock = stock;
        this.minimumStock = 0;
    }

    public Product(Long id, String name, String description, String category, BigDecimal price, Integer stock, Integer minimumStock) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.category = category;
        this.price = price;
        this.stock = stock;
        this.minimumStock = 0;
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
        return "Product{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", category='" + category + '\'' +
                ", price=" + price +
                ", stock=" + stock +
                ", minimumStock=" + minimumStock +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Product product = (Product) o;
        return id != null && id.equals(product.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    public boolean isLowStock() {
        return stock != null && minimumStock != null && stock <= minimumStock;
    }

    public boolean isOutOfStock() {
        return stock == null || stock == 0;
    }

    /*
    public void updateStock(Integer newStock, String updatedBy) {
        this.stock = newStock;
        this.updatedBy = updatedBy;
    }

    public void adjustStock(Integer quantity, String updatedBy) {
        this.stock = (this.stock != null ? this.stock : 0) + quantity;
        this.updatedBy = updatedBy;
    }
    */
}
