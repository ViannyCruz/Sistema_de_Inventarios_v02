package com.sistema_de_inventarios_v02.dto.api;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public class StockMovementDTO {

    @NotNull(message = "Product ID es requerido")
    private Long productId;

    @NotBlank(message = "Tipo de movimiento es requerido")
    @Pattern(regexp = "ENTRADA|SALIDA", message = "Tipo de movimiento debe ser ENTRADA o SALIDA")
    private String movementType;

    @NotNull(message = "Cantidad es requerida")
    @Min(value = 1, message = "La cantidad debe ser mayor a 0")
    private Integer quantity;

    private String reason;
    private String notes;
    private String userResponsible;

    // Constructores
    public StockMovementDTO() {}

    public StockMovementDTO(Long productId, String movementType, Integer quantity, String reason) {
        this.productId = productId;
        this.movementType = movementType;
        this.quantity = quantity;
        this.reason = reason;
    }

    // Getters y Setters
    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public String getMovementType() {
        return movementType;
    }

    public void setMovementType(String movementType) {
        this.movementType = movementType;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getUserResponsible() {
        return userResponsible;
    }

    public void setUserResponsible(String userResponsible) {
        this.userResponsible = userResponsible;
    }
}
