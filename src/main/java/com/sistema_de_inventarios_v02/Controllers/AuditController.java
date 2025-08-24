package com.sistema_de_inventarios_v02.Controllers;

import com.sistema_de_inventarios_v02.service.AuditService;
import com.sistema_de_inventarios_v02.dto.AuditRecordDTO;
import com.sistema_de_inventarios_v02.dto.ChangeDetailsDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/auditoria")
@CrossOrigin(origins = "*")
public class AuditController {

    @Autowired
    private AuditService auditService;


    // AGREGAR ESTE MÉTODO AL AuditController EXISTENTE

    /**
     * NUEVO ENDPOINT: Solo movimientos de stock
     * MANTENER todos los endpoints existentes
     */
    @GetMapping("/products/stock-movements")
    public ResponseEntity<Page<AuditRecordDTO>> getStockMovements(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "revision,desc") String sort,
            @RequestParam(required = false) String productName,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateTo) {

        try {
            String[] sortParams = sort.split(",");
            Sort.Direction direction = sortParams.length > 1 && "desc".equalsIgnoreCase(sortParams[1])
                    ? Sort.Direction.DESC : Sort.Direction.ASC;

            Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortParams[0]));

            // USAR EL NUEVO MÉTODO QUE FILTRA SOLO STOCK
            Page<AuditRecordDTO> stockMovements = auditService.getStockMovements(
                    pageable, productName, username, dateFrom, dateTo);

            return ResponseEntity.ok(stockMovements);
        } catch (Exception e) {
            System.err.println("Error en endpoint stock-movements: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Obtiene registros de auditoría paginados con filtros opcionales
     */
    @GetMapping("/products")
    public ResponseEntity<Page<AuditRecordDTO>> getProductAuditHistory(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "revision,desc") String sort,
            @RequestParam(required = false) String productName,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) Integer revType,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateTo) {

        try {
            // Parsear el parámetro de ordenamiento
            String[] sortParams = sort.split(",");
            Sort.Direction direction = sortParams.length > 1 && "desc".equalsIgnoreCase(sortParams[1])
                    ? Sort.Direction.DESC : Sort.Direction.ASC;

            Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortParams[0]));

            Page<AuditRecordDTO> auditRecords = auditService.getProductAuditHistory(
                    pageable, productName, username, revType, dateFrom, dateTo);

            return ResponseEntity.ok(auditRecords);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Obtiene detalles específicos de cambios para una revisión
     */
    @GetMapping("/products/{productId}/revision/{revision}/changes")
    public ResponseEntity<List<ChangeDetailsDTO>> getRevisionChanges(
            @PathVariable Long productId,
            @PathVariable Integer revision) {

        try {
            List<ChangeDetailsDTO> changes = auditService.getRevisionChanges(productId, revision);
            return ResponseEntity.ok(changes);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Obtiene el historial completo de un producto específico
     */
    @GetMapping("/products/{productId}/history")
    public ResponseEntity<Page<AuditRecordDTO>> getProductHistory(
            @PathVariable Long productId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "revision"));
            Page<AuditRecordDTO> history = auditService.getProductHistory(productId, pageable);
            return ResponseEntity.ok(history);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Exporta datos de auditoría a CSV
     */
    @GetMapping("/products/export")
    public ResponseEntity<byte[]> exportAuditData(
            @RequestParam(required = false) String productName,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) Integer revType,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateTo) {

        try {
            byte[] csvData = auditService.exportAuditDataToCsv(productName, username, revType, dateFrom, dateTo);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", "audit_report_" +
                    LocalDateTime.now().toString().replaceAll(":", "-") + ".csv");

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(csvData);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Obtiene estadísticas de auditoría
     */
    @GetMapping("/products/stats")
    public ResponseEntity<?> getAuditStats(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateTo) {

        try {
            var stats = auditService.getAuditStatistics(dateFrom, dateTo);
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}