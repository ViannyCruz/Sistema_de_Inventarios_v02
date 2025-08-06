package com.sistema_de_inventarios_v02.dto;

import java.time.LocalDateTime; /**
 * DTO para estadísticas de auditoría
 */
public class AuditStatisticsDTO {
    private long totalRecords;
    private long creationCount;
    private long modificationCount;
    private long deletionCount;
    private String mostActiveUser;
    private String mostModifiedProduct;
    private LocalDateTime periodStart;
    private LocalDateTime periodEnd;

    // Constructores
    public AuditStatisticsDTO() {}

    public AuditStatisticsDTO(long totalRecords, long creationCount, long modificationCount, 
                             long deletionCount, String mostActiveUser, String mostModifiedProduct,
                             LocalDateTime periodStart, LocalDateTime periodEnd) {
        this.totalRecords = totalRecords;
        this.creationCount = creationCount;
        this.modificationCount = modificationCount;
        this.deletionCount = deletionCount;
        this.mostActiveUser = mostActiveUser;
        this.mostModifiedProduct = mostModifiedProduct;
        this.periodStart = periodStart;
        this.periodEnd = periodEnd;
    }

    // Getters y Setters
    public long getTotalRecords() {
        return totalRecords;
    }

    public void setTotalRecords(long totalRecords) {
        this.totalRecords = totalRecords;
    }

    public long getCreationCount() {
        return creationCount;
    }

    public void setCreationCount(long creationCount) {
        this.creationCount = creationCount;
    }

    public long getModificationCount() {
        return modificationCount;
    }

    public void setModificationCount(long modificationCount) {
        this.modificationCount = modificationCount;
    }

    public long getDeletionCount() {
        return deletionCount;
    }

    public void setDeletionCount(long deletionCount) {
        this.deletionCount = deletionCount;
    }

    public String getMostActiveUser() {
        return mostActiveUser;
    }

    public void setMostActiveUser(String mostActiveUser) {
        this.mostActiveUser = mostActiveUser;
    }

    public String getMostModifiedProduct() {
        return mostModifiedProduct;
    }

    public void setMostModifiedProduct(String mostModifiedProduct) {
        this.mostModifiedProduct = mostModifiedProduct;
    }

    public LocalDateTime getPeriodStart() {
        return periodStart;
    }

    public void setPeriodStart(LocalDateTime periodStart) {
        this.periodStart = periodStart;
    }

    public LocalDateTime getPeriodEnd() {
        return periodEnd;
    }

    public void setPeriodEnd(LocalDateTime periodEnd) {
        this.periodEnd = periodEnd;
    }
}
