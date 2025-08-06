package com.sistema_de_inventarios_v02.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;

/**
 * DTO para representar un registro de auditoría
 */
public class AuditRecordDTO {
    private Integer revision;
    private Long productId;
    private String productName;
    private Integer revType;
    private String username;
    private Long userId;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime revisionDate;

    private int changesCount;

    // Constructores
    public AuditRecordDTO() {}

    public AuditRecordDTO(Integer revision, Long productId, String productName,
                          Integer revType, String username, Long userId,
                          LocalDateTime revisionDate, int changesCount) {
        this.revision = revision;
        this.productId = productId;
        this.productName = productName;
        this.revType = revType;
        this.username = username;
        this.userId = userId;
        this.revisionDate = revisionDate;
        this.changesCount = changesCount;
    }

    // Getters y Setters
    public Integer getRevision() {
        return revision;
    }

    public void setRevision(Integer revision) {
        this.revision = revision;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public Integer getRevType() {
        return revType;
    }

    public void setRevType(Integer revType) {
        this.revType = revType;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public LocalDateTime getRevisionDate() {
        return revisionDate;
    }

    public void setRevisionDate(LocalDateTime revisionDate) {
        this.revisionDate = revisionDate;
    }

    public int getChangesCount() {
        return changesCount;
    }

    public void setChangesCount(int changesCount) {
        this.changesCount = changesCount;
    }

    /**
     * Obtiene el nombre de la operación basado en revType
     */
    public String getOperationName() {
        switch (revType) {
            case 0: return "Creación";
            case 1: return "Modificación";
            case 2: return "Eliminación";
            default: return "Desconocido";
        }
    }
}