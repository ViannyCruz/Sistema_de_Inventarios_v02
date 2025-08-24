package com.sistema_de_inventarios_v02.dto;

/**
 * DTO para representar los detalles de cambios en una revisión
 */
public class ChangeDetailsDTO {
    private String fieldName;
    private String oldValue;
    private String newValue;
    private String changeType;

    // Constructores
    public ChangeDetailsDTO() {}

    public ChangeDetailsDTO(String fieldName, String oldValue, String newValue, String changeType) {
        this.fieldName = fieldName;
        this.oldValue = oldValue;
        this.newValue = newValue;
        this.changeType = changeType;
    }

    // Getters y Setters
    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public String getOldValue() {
        return oldValue;
    }

    public void setOldValue(String oldValue) {
        this.oldValue = oldValue;
    }

    public String getNewValue() {
        return newValue;
    }

    public void setNewValue(String newValue) {
        this.newValue = newValue;
    }

    public String getChangeType() {
        return changeType;
    }

    public void setChangeType(String changeType) {
        this.changeType = changeType;
    }

    /**
     * Obtiene una descripción legible del campo
     */
    public String getFieldDisplayName() {
        switch (fieldName) {
            case "name": return "Nombre";
            case "description": return "Descripción";
            case "category": return "Categoría";
            case "price": return "Precio";
            case "stock": return "Stock";
            case "minimumStock": return "Stock Mínimo";
            case "createdAt": return "Fecha de Creación";
            case "updatedAt": return "Fecha de Actualización";
            default: return fieldName;
        }
    }
}
