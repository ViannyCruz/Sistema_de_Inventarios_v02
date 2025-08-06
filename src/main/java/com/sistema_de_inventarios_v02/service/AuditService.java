package com.sistema_de_inventarios_v02.service;

import com.sistema_de_inventarios_v02.dto.AuditRecordDTO;
import com.sistema_de_inventarios_v02.dto.ChangeDetailsDTO;
import com.sistema_de_inventarios_v02.dto.AuditStatisticsDTO;
import com.sistema_de_inventarios_v02.audit.CustomRevisionEntity;
import com.sistema_de_inventarios_v02.model.Product; // Asegúrate de que esta sea la ruta correcta
import org.hibernate.envers.AuditReader;
import org.hibernate.envers.AuditReaderFactory;
import org.hibernate.envers.RevisionType;
import org.hibernate.envers.query.AuditEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class AuditService {

    @PersistenceContext
    private EntityManager entityManager;

    /**
     * Obtiene el historial de auditoría con filtros y paginación
     */
    public Page<AuditRecordDTO> getProductAuditHistory(Pageable pageable, String productName,
                                                       String username, Integer revType,
                                                       LocalDateTime dateFrom, LocalDateTime dateTo) {

        try {
            AuditReader auditReader = AuditReaderFactory.get(entityManager);

            // Construir consulta con filtros
            var query = auditReader.createQuery()
                    .forRevisionsOfEntity(Product.class, false, true);

            // Aplicar filtros
            if (productName != null && !productName.trim().isEmpty()) {
                query.add(AuditEntity.property("name").like("%" + productName.trim() + "%"));
            }

            if (revType != null) {
                query.add(AuditEntity.revisionType().eq(RevisionType.values()[revType]));
            }

            if (dateFrom != null) {
                long timestampFrom = dateFrom.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
                query.add(AuditEntity.revisionProperty("timestamp").ge(timestampFrom));
            }

            if (dateTo != null) {
                long timestampTo = dateTo.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
                query.add(AuditEntity.revisionProperty("timestamp").le(timestampTo));
            }

            if (username != null && !username.trim().isEmpty()) {
                query.add(AuditEntity.revisionProperty("username").like("%" + username.trim() + "%"));
            }

            // Agregar ordenamiento
            query.addOrder(AuditEntity.revisionNumber().desc());

            // Obtener resultados
            @SuppressWarnings("unchecked")
            List<Object[]> results = query.getResultList();

            // Convertir a DTOs
            List<AuditRecordDTO> auditRecords = results.stream()
                    .map(this::convertToAuditRecordDTO)
                    .collect(Collectors.toList());

            // Aplicar paginación manual
            int start = (int) pageable.getOffset();
            int end = Math.min((start + pageable.getPageSize()), auditRecords.size());

            if (start >= auditRecords.size()) {
                return new PageImpl<>(Collections.emptyList(), pageable, auditRecords.size());
            }

            List<AuditRecordDTO> pageContent = auditRecords.subList(start, end);

            return new PageImpl<>(pageContent, pageable, auditRecords.size());

        } catch (Exception e) {
            // Log del error para debugging
            System.err.println("Error en getProductAuditHistory: " + e.getMessage());
            e.printStackTrace();

            // Devolver página vacía en caso de error
            return new PageImpl<>(Collections.emptyList(), pageable, 0);
        }
    }

    /**
     * Obtiene detalles de cambios para una revisión específica
     */
    public List<ChangeDetailsDTO> getRevisionChanges(Long productId, Integer revision) {
        try {
            AuditReader auditReader = AuditReaderFactory.get(entityManager);

            // Obtener la entidad en la revisión actual
            Product currentRevision = auditReader.find(Product.class, productId, revision);

            if (currentRevision == null) {
                return Collections.emptyList();
            }

            // Obtener la revisión anterior para comparar
            List<Number> revisions = auditReader.getRevisions(Product.class, productId);
            Product previousRevision = null;

            for (int i = 0; i < revisions.size(); i++) {
                if (revisions.get(i).intValue() == revision && i > 0) {
                    previousRevision = auditReader.find(Product.class, productId,
                            revisions.get(i - 1));
                    break;
                }
            }

            return compareRevisions(previousRevision, currentRevision, revision);

        } catch (Exception e) {
            System.err.println("Error en getRevisionChanges: " + e.getMessage());
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    /**
     * Obtiene el historial completo de un producto específico
     */
    public Page<AuditRecordDTO> getProductHistory(Long productId, Pageable pageable) {
        try {
            AuditReader auditReader = AuditReaderFactory.get(entityManager);

            var query = auditReader.createQuery()
                    .forRevisionsOfEntity(Product.class, false, true)
                    .add(AuditEntity.id().eq(productId))
                    .addOrder(AuditEntity.revisionNumber().desc());

            @SuppressWarnings("unchecked")
            List<Object[]> results = query.getResultList();

            List<AuditRecordDTO> auditRecords = results.stream()
                    .map(this::convertToAuditRecordDTO)
                    .collect(Collectors.toList());

            // Aplicar paginación
            int start = (int) pageable.getOffset();
            int end = Math.min((start + pageable.getPageSize()), auditRecords.size());

            if (start >= auditRecords.size()) {
                return new PageImpl<>(Collections.emptyList(), pageable, auditRecords.size());
            }

            List<AuditRecordDTO> pageContent = auditRecords.subList(start, end);

            return new PageImpl<>(pageContent, pageable, auditRecords.size());

        } catch (Exception e) {
            System.err.println("Error en getProductHistory: " + e.getMessage());
            e.printStackTrace();
            return new PageImpl<>(Collections.emptyList(), pageable, 0);
        }
    }

    /**
     * Exporta datos de auditoría a CSV
     */
    public byte[] exportAuditDataToCsv(String productName, String username, Integer revType,
                                       LocalDateTime dateFrom, LocalDateTime dateTo) {

        try {
            // Obtener todos los datos sin paginación
            var allData = getProductAuditHistory(
                    Pageable.unpaged(), productName, username, revType, dateFrom, dateTo);

            try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
                 PrintWriter writer = new PrintWriter(baos)) {

                // Escribir encabezados CSV
                writer.println("Revisión,Fecha/Hora,Usuario,Producto ID,Nombre Producto,Operación,Cambios");

                // Escribir datos
                for (AuditRecordDTO record : allData.getContent()) {
                    writer.printf("%d,%s,%s,%d,%s,%s,%d%n",
                            record.getRevision(),
                            record.getRevisionDate(),
                            record.getUsername() != null ? record.getUsername() : "Sistema",
                            record.getProductId(),
                            record.getProductName() != null ? record.getProductName() : "N/A",
                            record.getOperationName(),
                            record.getChangesCount()
                    );
                }

                writer.flush();
                return baos.toByteArray();

            }
        } catch (Exception e) {
            System.err.println("Error en exportAuditDataToCsv: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Error al generar CSV", e);
        }
    }

    /**
     * Obtiene estadísticas de auditoría
     */
    public AuditStatisticsDTO getAuditStatistics(LocalDateTime dateFrom, LocalDateTime dateTo) {
        try {
            // Obtener datos para estadísticas
            var allData = getProductAuditHistory(Pageable.unpaged(), null, null, null, dateFrom, dateTo);

            List<AuditRecordDTO> records = allData.getContent();

            long totalRecords = records.size();
            long creationCount = records.stream().filter(r -> r.getRevType() == 0).count();
            long modificationCount = records.stream().filter(r -> r.getRevType() == 1).count();
            long deletionCount = records.stream().filter(r -> r.getRevType() == 2).count();

            // Usuario más activo
            String mostActiveUser = records.stream()
                    .filter(r -> r.getUsername() != null)
                    .collect(Collectors.groupingBy(AuditRecordDTO::getUsername, Collectors.counting()))
                    .entrySet().stream()
                    .max(Map.Entry.comparingByValue())
                    .map(Map.Entry::getKey)
                    .orElse("N/A");

            // Producto más modificado
            String mostModifiedProduct = records.stream()
                    .filter(r -> r.getProductName() != null)
                    .collect(Collectors.groupingBy(AuditRecordDTO::getProductName, Collectors.counting()))
                    .entrySet().stream()
                    .max(Map.Entry.comparingByValue())
                    .map(Map.Entry::getKey)
                    .orElse("N/A");

            return new AuditStatisticsDTO(totalRecords, creationCount, modificationCount,
                    deletionCount, mostActiveUser, mostModifiedProduct, dateFrom, dateTo);

        } catch (Exception e) {
            System.err.println("Error en getAuditStatistics: " + e.getMessage());
            e.printStackTrace();
            return new AuditStatisticsDTO(0, 0, 0, 0, "N/A", "N/A", dateFrom, dateTo);
        }
    }

    /**
     * Convierte resultado de consulta a DTO
     */
    private AuditRecordDTO convertToAuditRecordDTO(Object[] result) {
        try {
            Product product = (Product) result[0];
            CustomRevisionEntity revision = (CustomRevisionEntity) result[1];
            RevisionType revType = (RevisionType) result[2];

            LocalDateTime revisionDate = LocalDateTime.ofInstant(
                    java.time.Instant.ofEpochMilli(revision.getTimestamp()),
                    ZoneId.systemDefault());

            // Calcular número de cambios (simplificado)
            int changesCount = calculateChangesCount(product, revision.getId());

            // Convertir Byte a Integer para el revType
            Integer revTypeInt = revType.getRepresentation().intValue();

            return new AuditRecordDTO(
                    revision.getId(),
                    product.getId(),
                    product.getName(),
                    revTypeInt,  // Ahora es Integer
                    revision.getUsername(),
                    revision.getUserId(),
                    revisionDate,
                    changesCount
            );
        } catch (Exception e) {
            System.err.println("Error en convertToAuditRecordDTO: " + e.getMessage());
            e.printStackTrace();
            // Devolver un DTO con valores por defecto
            return new AuditRecordDTO(0, 0L, "Error", 0, "Error", 0L, LocalDateTime.now(), 0);
        }
    }

    /**
     * Compara dos revisiones para detectar cambios
     */
    private List<ChangeDetailsDTO> compareRevisions(Product previous, Product current, Integer revision) {
        List<ChangeDetailsDTO> changes = new ArrayList<>();

        try {
            if (previous == null) {
                // Primera revisión - todos los campos son nuevos
                addChangeIfNotNull(changes, "name", null, current.getName(), "ADDED");
                addChangeIfNotNull(changes, "description", null, current.getDescription(), "ADDED");
                addChangeIfNotNull(changes, "category", null, current.getCategory(), "ADDED");
                addChangeIfNotNull(changes, "price", null, current.getPrice(), "ADDED");
                addChangeIfNotNull(changes, "stock", null, current.getStock(), "ADDED");
                if (current.getMinimumStock() != null) {
                    addChangeIfNotNull(changes, "minimumStock", null, current.getMinimumStock(), "ADDED");
                }
            } else {
                // Comparar campos
                compareField(changes, "name", previous.getName(), current.getName());
                compareField(changes, "description", previous.getDescription(), current.getDescription());
                compareField(changes, "category", previous.getCategory(), current.getCategory());
                compareField(changes, "price", previous.getPrice(), current.getPrice());
                compareField(changes, "stock", previous.getStock(), current.getStock());
                compareField(changes, "minimumStock", previous.getMinimumStock(), current.getMinimumStock());
            }
        } catch (Exception e) {
            System.err.println("Error en compareRevisions: " + e.getMessage());
            e.printStackTrace();
        }

        return changes;
    }

    private void compareField(List<ChangeDetailsDTO> changes, String fieldName, Object oldValue, Object newValue) {
        if (!Objects.equals(oldValue, newValue)) {
            changes.add(new ChangeDetailsDTO(fieldName,
                    oldValue != null ? oldValue.toString() : null,
                    newValue != null ? newValue.toString() : null,
                    "MODIFIED"));
        }
    }

    private void addChangeIfNotNull(List<ChangeDetailsDTO> changes, String fieldName,
                                    Object oldValue, Object newValue, String changeType) {
        if (newValue != null) {
            changes.add(new ChangeDetailsDTO(fieldName,
                    oldValue != null ? oldValue.toString() : null,
                    newValue.toString(),
                    changeType));
        }
    }

    /**
     * Calcula el número de cambios en una revisión (simplificado)
     */
    private int calculateChangesCount(Product product, Integer revision) {
        // Esta es una implementación simplificada
        return 1; // Por ahora retorna 1 para indicar que hubo al menos un cambio
    }

    // AGREGAR ESTOS MÉTODOS AL AuditService EXISTENTE (no reemplazar)

    /**
     * NUEVO MÉTODO: Obtiene SOLO los registros que cambiaron el stock
     * MANTENER todos los métodos existentes
     */
    public Page<AuditRecordDTO> getStockMovements(Pageable pageable, String productName,
                                                  String username, LocalDateTime dateFrom,
                                                  LocalDateTime dateTo) {
        try {
            AuditReader auditReader = AuditReaderFactory.get(entityManager);

            // Obtener TODAS las revisiones primero
            var query = auditReader.createQuery()
                    .forRevisionsOfEntity(Product.class, false, true);

            // Aplicar filtros básicos
            if (productName != null && !productName.trim().isEmpty()) {
                query.add(AuditEntity.property("name").like("%" + productName.trim() + "%"));
            }

            if (dateFrom != null) {
                long timestampFrom = dateFrom.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
                query.add(AuditEntity.revisionProperty("timestamp").ge(timestampFrom));
            }

            if (dateTo != null) {
                long timestampTo = dateTo.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
                query.add(AuditEntity.revisionProperty("timestamp").le(timestampTo));
            }

            if (username != null && !username.trim().isEmpty()) {
                query.add(AuditEntity.revisionProperty("username").like("%" + username.trim() + "%"));
            }

            query.addOrder(AuditEntity.revisionNumber().desc());

            @SuppressWarnings("unchecked")
            List<Object[]> results = query.getResultList();

            // FILTRAR SOLO LOS QUE CAMBIARON STOCK
            List<AuditRecordDTO> stockMovements = results.stream()
                    .map(this::convertToStockAuditRecordDTO) // Nuevo método para stock
                    .filter(this::isStockMovement)
                    .collect(Collectors.toList());

            // Aplicar paginación manual
            int start = (int) pageable.getOffset();
            int end = Math.min((start + pageable.getPageSize()), stockMovements.size());

            if (start >= stockMovements.size()) {
                return new PageImpl<>(Collections.emptyList(), pageable, stockMovements.size());
            }

            List<AuditRecordDTO> pageContent = stockMovements.subList(start, end);
            return new PageImpl<>(pageContent, pageable, stockMovements.size());

        } catch (Exception e) {
            System.err.println("Error en getStockMovements: " + e.getMessage());
            e.printStackTrace();
            return new PageImpl<>(Collections.emptyList(), pageable, 0);
        }
    }

    /**
     * NUEVO MÉTODO: Detecta si una revisión cambió el stock
     */
    private boolean isStockMovement(AuditRecordDTO record) {
        try {
            // Si es creación o eliminación, SI es movimiento de stock
            if (record.getRevType() == 0 || record.getRevType() == 2) {
                return true;
            }

            // Si es actualización (revType = 1), verificar si cambió el stock
            if (record.getRevType() == 1) {
                return didStockChange(record.getProductId(), record.getRevision());
            }

            return false;
        } catch (Exception e) {
            System.err.println("Error verificando movimiento de stock: " + e.getMessage());
            return false;
        }
    }

    /**
     * NUEVO MÉTODO: Verifica si el campo 'stock' cambió en esta revisión específica
     */
    private boolean didStockChange(Long productId, Integer revision) {
        try {
            AuditReader auditReader = AuditReaderFactory.get(entityManager);

            // Obtener la entidad en esta revisión
            Product currentRevision = auditReader.find(Product.class, productId, revision);
            if (currentRevision == null) return false;

            // Obtener todas las revisiones de este producto
            List<Number> revisions = auditReader.getRevisions(Product.class, productId);

            // Encontrar la revisión anterior
            Product previousRevision = null;
            for (int i = 0; i < revisions.size(); i++) {
                if (revisions.get(i).intValue() == revision && i > 0) {
                    previousRevision = auditReader.find(Product.class, productId,
                            revisions.get(i - 1));
                    break;
                }
            }

            // Si no hay revisión anterior, considerar como movimiento (primera vez)
            if (previousRevision == null) {
                return true;
            }

            // COMPARAR SOLO EL STOCK
            Integer previousStock = previousRevision.getStock();
            Integer currentStock = currentRevision.getStock();

            // ¿Cambió el stock?
            boolean stockChanged = !Objects.equals(previousStock, currentStock);

            if (stockChanged) {
                System.out.println("STOCK CAMBIÓ - Producto: " + productId +
                        " | Anterior: " + previousStock + " | Actual: " + currentStock);
            }

            return stockChanged;

        } catch (Exception e) {
            System.err.println("Error verificando cambio de stock: " + e.getMessage());
            return false;
        }
    }

    /**
     * NUEVO MÉTODO: Convierte resultado específicamente para movimientos de stock
     */
    private AuditRecordDTO convertToStockAuditRecordDTO(Object[] result) {
        try {
            Product product = (Product) result[0];
            CustomRevisionEntity revision = (CustomRevisionEntity) result[1];
            RevisionType revType = (RevisionType) result[2];

            LocalDateTime revisionDate = LocalDateTime.ofInstant(
                    java.time.Instant.ofEpochMilli(revision.getTimestamp()),
                    ZoneId.systemDefault());

            // Obtener información del cambio de stock para mostrar
            String stockInfo = getStockChangeInfo(product.getId(), revision.getId(), revType);
            String productNameWithStock = product.getName() +
                    (stockInfo != null ? " - " + stockInfo : "");

            Integer revTypeInt = revType.getRepresentation().intValue();

            return new AuditRecordDTO(
                    revision.getId(),
                    product.getId(),
                    productNameWithStock, // ← Incluye info del stock
                    revTypeInt,
                    revision.getUsername(),
                    revision.getUserId(),
                    revisionDate,
                    1 // changesCount simplificado
            );
        } catch (Exception e) {
            System.err.println("Error en convertToStockAuditRecordDTO: " + e.getMessage());
            return new AuditRecordDTO(0, 0L, "Error", 0, "Error", 0L, LocalDateTime.now(), 0);
        }
    }

    /**
     * NUEVO MÉTODO: Obtiene información legible del cambio de stock
     */
    private String getStockChangeInfo(Long productId, Integer revision, RevisionType revType) {
        try {
            if (revType == RevisionType.ADD) {
                return "Stock inicial: " + getCurrentStock(productId, revision);
            }

            if (revType == RevisionType.DEL) {
                return "Stock eliminado: " + getCurrentStock(productId, revision);
            }

            if (revType == RevisionType.MOD) {
                AuditReader auditReader = AuditReaderFactory.get(entityManager);
                Product current = auditReader.find(Product.class, productId, revision);

                List<Number> revisions = auditReader.getRevisions(Product.class, productId);
                Product previous = null;

                for (int i = 0; i < revisions.size(); i++) {
                    if (revisions.get(i).intValue() == revision && i > 0) {
                        previous = auditReader.find(Product.class, productId, revisions.get(i - 1));
                        break;
                    }
                }

                if (previous != null && current != null) {
                    return "Stock: " + previous.getStock() + " → " + current.getStock();
                }
            }

            return null;
        } catch (Exception e) {
            return null;
        }
    }


    private Integer getCurrentStock(Long productId, Integer revision) {
        try {
            AuditReader auditReader = AuditReaderFactory.get(entityManager);
            Product product = auditReader.find(Product.class, productId, revision);
            return product != null ? product.getStock() : 0;
        } catch (Exception e) {
            return 0;
        }
    }
}