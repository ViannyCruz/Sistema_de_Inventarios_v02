package com.sistema_de_inventarios_v02.dto;

import java.math.BigDecimal;

public class ProductSummaryDTO {
    
    private Long id;
    private String name;
    private String category;
    private BigDecimal price;
    private Integer stock;
    private String stockStatus;

    // Constructores
    public ProductSummaryDTO() {}

    public ProductSummaryDTO(Long id, String name, String category, 
                            BigDecimal price, Integer stock, String stockStatus) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.price = price;
        this.stock = stock;
        this.stockStatus = stockStatus;
    }

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    
    public Integer getStock() { return stock; }
    public void setStock(Integer stock) { this.stock = stock; }
    
    public String getStockStatus() { return stockStatus; }
    public void setStockStatus(String stockStatus) { this.stockStatus = stockStatus; }
}