package com.sistema_de_inventarios_v02.dto;
import jakarta.validation.constraints.*;

public class StockUpdateDTO {
    
    @NotNull(message = "El stock es obligatorio")
    @Min(value = 0, message = "El stock no puede ser negativo")
    private Integer stock;
    
    private String reason; // Motivo del ajuste de stock

    // Constructores
    public StockUpdateDTO() {}

    public StockUpdateDTO(Integer stock, String reason) {
        this.stock = stock;
        this.reason = reason;
    }

    // Getters y Setters
    public Integer getStock() { return stock; }
    public void setStock(Integer stock) { this.stock = stock; }
    
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}