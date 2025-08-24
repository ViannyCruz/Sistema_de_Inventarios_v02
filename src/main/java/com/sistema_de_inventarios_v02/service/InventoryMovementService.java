package com.sistema_de_inventarios_v02.service;

import com.sistema_de_inventarios_v02.model.Product;
import com.sistema_de_inventarios_v02.audit.CustomRevisionEntity;
import jakarta.persistence.EntityManager;
import org.hibernate.envers.AuditReader;
import org.hibernate.envers.AuditReaderFactory;
import org.hibernate.envers.RevisionType;
import org.hibernate.envers.query.AuditEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class InventoryMovementService {
    
    @Autowired
    private EntityManager entityManager;
    
    /**
     * DTO para representar un movimiento de inventario
     */
    public static class InventoryMovement {
        private Long productId;
        private String productName;
        private String movementType; // "ENTRADA" o "SALIDA"
        private Integer quantity;
        private Integer previousStock;
        private Integer newStock;
        private String username;
        private Long userId;
        private LocalDateTime movementDate;
        private Number revision;
        
        // Constructor
        public InventoryMovement(Long productId, String productName, String movementType, 
                               Integer quantity, Integer previousStock, Integer newStock, 
                               String username, Long userId, LocalDateTime movementDate, Number revision) {
            this.productId = productId;
            this.productName = productName;
            this.movementType = movementType;
            this.quantity = quantity;
            this.previousStock = previousStock;
            this.newStock = newStock;
            this.username = username;
            this.userId = userId;
            this.movementDate = movementDate;
            this.revision = revision;
        }
        
        // Getters y setters
        public Long getProductId() { return productId; }
        public String getProductName() { return productName; }
        public String getMovementType() { return movementType; }
        public Integer getQuantity() { return quantity; }
        public Integer getPreviousStock() { return previousStock; }
        public Integer getNewStock() { return newStock; }
        public String getUsername() { return username; }
        public LocalDateTime getMovementDate() { return movementDate; }
        public Number getRevision() { return revision; }
        
        public void setProductId(Long productId) { this.productId = productId; }
        public void setProductName(String productName) { this.productName = productName; }
        public void setMovementType(String movementType) { this.movementType = movementType; }
        public void setQuantity(Integer quantity) { this.quantity = quantity; }
        public void setPreviousStock(Integer previousStock) { this.previousStock = previousStock; }
        public void setNewStock(Integer newStock) { this.newStock = newStock; }
        public void setUsername(String username) { this.username = username; }
        public void setMovementDate(LocalDateTime movementDate) { this.movementDate = movementDate; }
        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }
    }
    
    /**
     * Obtiene todos los movimientos de inventario (entradas y salidas)
     */
    @SuppressWarnings("unchecked")
    public List<InventoryMovement> getAllInventoryMovements() {
        AuditReader auditReader = AuditReaderFactory.get(entityManager);
        
        List<Object[]> auditResults = auditReader.createQuery()
                .forRevisionsOfEntity(Product.class, false, true)
                .addOrder(AuditEntity.revisionNumber().desc())
                .getResultList();
        
        return processAuditResults(auditResults);
    }
    
    /**
     * Obtiene movimientos de inventario por producto
     */
    @SuppressWarnings("unchecked")
    public List<InventoryMovement> getInventoryMovementsByProduct(Long productId) {
        AuditReader auditReader = AuditReaderFactory.get(entityManager);
        
        List<Object[]> auditResults = auditReader.createQuery()
                .forRevisionsOfEntity(Product.class, false, true)
                .add(AuditEntity.id().eq(productId))
                .addOrder(AuditEntity.revisionNumber().desc())
                .getResultList();
        
        return processAuditResults(auditResults);
    }
    
    /**
     * Obtiene movimientos de inventario en un rango de fechas
     */
    @SuppressWarnings("unchecked")
    public List<InventoryMovement> getInventoryMovementsBetweenDates(LocalDateTime startDate, LocalDateTime endDate) {
        AuditReader auditReader = AuditReaderFactory.get(entityManager);
        
        Date start = Date.from(startDate.atZone(ZoneId.systemDefault()).toInstant());
        Date end = Date.from(endDate.atZone(ZoneId.systemDefault()).toInstant());
        
        List<Object[]> auditResults = auditReader.createQuery()
                .forRevisionsOfEntity(Product.class, false, true)
                .add(AuditEntity.revisionProperty("timestamp").between(start.getTime(), end.getTime()))
                .addOrder(AuditEntity.revisionNumber().desc())
                .getResultList();
        
        return processAuditResults(auditResults);
    }
    
    /**
     * Obtiene solo las entradas de productos
     */
    public List<InventoryMovement> getProductEntries() {
        return getAllInventoryMovements().stream()
                .filter(movement -> "ENTRADA".equals(movement.getMovementType()))
                .collect(Collectors.toList());
    }
    
    /**
     * Obtiene solo las salidas de productos
     */
    public List<InventoryMovement> getProductExits() {
        return getAllInventoryMovements().stream()
                .filter(movement -> "SALIDA".equals(movement.getMovementType()))
                .collect(Collectors.toList());
    }
    
    /**
     * Obtiene movimientos por usuario responsable
     */
    public List<InventoryMovement> getInventoryMovementsByUser(String username) {
        return getAllInventoryMovements().stream()
                .filter(movement -> username.equals(movement.getUsername()))
                .collect(Collectors.toList());
    }
    
    /**
     * Procesa los resultados de auditoría y los convierte en movimientos de inventario
     */
    private List<InventoryMovement> processAuditResults(List<Object[]> auditResults) {
        List<InventoryMovement> movements = new ArrayList<>();
        Map<Long, Integer> previousStockMap = new HashMap<>();
        
        // Procesar en orden cronológico inverso para calcular stocks anteriores
        for (int i = auditResults.size() - 1; i >= 0; i--) {
            Object[] result = auditResults.get(i);
            
            Product product = (Product) result[0];
            CustomRevisionEntity revision = (CustomRevisionEntity) result[1];
            RevisionType revisionType = (RevisionType) result[2];
            
            // Solo procesar modificaciones que afecten el stock
            if (revisionType == RevisionType.MOD || revisionType == RevisionType.ADD) {
                Long productId = product.getId();
                Integer currentStock = product.getStock();
                Integer previousStock = previousStockMap.getOrDefault(productId, 0);
                
                // Solo crear movimiento si hubo cambio en el stock
                if (!currentStock.equals(previousStock) || revisionType == RevisionType.ADD) {
                    Integer quantity = Math.abs(currentStock - previousStock);
                    String movementType = currentStock > previousStock ? "ENTRADA" : "SALIDA";
                    
                    // Para productos nuevos, considerar como entrada
                    if (revisionType == RevisionType.ADD && currentStock > 0) {
                        movementType = "ENTRADA";
                        quantity = currentStock;
                        previousStock = 0;
                    }
                    
                    LocalDateTime movementDate = LocalDateTime.ofInstant(
                        new Date(revision.getTimestamp()).toInstant(),
                        ZoneId.systemDefault()
                    );
                    
                    String username = revision.getUsername() != null ? revision.getUsername() : "SYSTEM";
                    Long userId = revision.getUserId();
                    
                    InventoryMovement movement = new InventoryMovement(
                        productId,
                        product.getName(),
                        movementType,
                        quantity,
                        previousStock,
                        currentStock,
                        username,
                        userId,
                        movementDate,
                        revision.getId()
                    );
                    
                    movements.add(movement);
                }
                
                previousStockMap.put(productId, currentStock);
            }
        }
        
        // Devolver en orden cronológico descendente (más recientes primero)
        Collections.reverse(movements);
        return movements;
    }
}