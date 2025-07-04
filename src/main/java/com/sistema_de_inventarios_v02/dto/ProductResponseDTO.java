package com.sistema_de_inventarios_v02.dto;

import java.math.BigDecimal;

public class ProductResponseDTO {
    
    private Long id;
    private String name;
    private String description;
    private String category;
    private BigDecimal price;
    private Integer stock;
    private Integer minimumStock;
    private boolean isLowStock;
    private boolean isOutOfStock;
    private String stockStatus;

    // Constructores
    public ProductResponseDTO() {}

    public ProductResponseDTO(Long id, String name, String description, String category,
                             BigDecimal price, Integer stock, Integer minimumStock,
                             boolean isLowStock, boolean isOutOfStock, String stockStatus) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.category = category;
        this.price = price;
        this.stock = stock;
        this.minimumStock = minimumStock;
        this.isLowStock = isLowStock;
        this.isOutOfStock = isOutOfStock;
        this.stockStatus = stockStatus;
    }

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    
    public Integer getStock() { return stock; }
    public void setStock(Integer stock) { this.stock = stock; }
    
    public Integer getMinimumStock() { return minimumStock; }
    public void setMinimumStock(Integer minimumStock) { this.minimumStock = minimumStock; }
    
    public boolean isLowStock() { return isLowStock; }
    public void setLowStock(boolean lowStock) { isLowStock = lowStock; }
    
    public boolean isOutOfStock() { return isOutOfStock; }
    public void setOutOfStock(boolean outOfStock) { isOutOfStock = outOfStock; }
    
    public String getStockStatus() { return stockStatus; }
    public void setStockStatus(String stockStatus) { this.stockStatus = stockStatus; }
}