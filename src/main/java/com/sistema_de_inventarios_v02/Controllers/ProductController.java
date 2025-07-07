package com.sistema_de_inventarios_v02.Controllers;

import com.sistema_de_inventarios_v02.dto.CreateProductDTO;
import com.sistema_de_inventarios_v02.dto.UpdateProductDTO;
import com.sistema_de_inventarios_v02.dto.ProductResponseDTO;
import com.sistema_de_inventarios_v02.dto.ProductSummaryDTO;
import com.sistema_de_inventarios_v02.dto.StockUpdateDTO;
import com.sistema_de_inventarios_v02.service.ProductService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/products")
@CrossOrigin(origins = "*", allowedHeaders = "*")
@Validated
public class ProductController {

    private final ProductService productService;

    @Autowired
    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public ResponseEntity<ProductResponseDTO> createProduct(@Valid @RequestBody CreateProductDTO createProductDTO) {
        System.out.println("CREAR EL PRODUCTO");
        ProductResponseDTO createdProduct = productService.createProduct(createProductDTO);
        return new ResponseEntity<>(createdProduct, HttpStatus.CREATED);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER') or hasRole('VISITOR')")
    public ResponseEntity<List<ProductSummaryDTO>> getAllProducts() {
        List<ProductSummaryDTO> products = productService.getAllProductsSummary();
        return ResponseEntity.ok(products);
    }

    @GetMapping("/paginated")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER') or hasRole('VISITOR')")
    public ResponseEntity<Page<ProductSummaryDTO>> getProductsPaginated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {

        if (page < 0) {
            page = 0;
        }
        if (size <= 0 || size > 100) {
            size = 10;
        }

        Pageable pageable = PageRequest.of(page, size,
                sortDir.equalsIgnoreCase("desc") ?
                        Sort.by(sortBy).descending() :
                        Sort.by(sortBy).ascending());

        Page<ProductSummaryDTO> products = productService.getAllProductsPaginated(pageable);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER') or hasRole('VISITOR')")
    public ResponseEntity<ProductResponseDTO> getProductById(@PathVariable Long id) {
        ProductResponseDTO product = productService.getProductById(id);
        return ResponseEntity.ok(product);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public ResponseEntity<ProductResponseDTO> updateProduct(@PathVariable Long id,
                                                            @Valid @RequestBody UpdateProductDTO updateProductDTO) {
        ProductResponseDTO updatedProduct = productService.updateProduct(id, updateProductDTO);
        return ResponseEntity.ok(updatedProduct);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public ResponseEntity<Map<String, Object>> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.ok(Map.of(
                "message", "Producto eliminado exitosamente",
                "timestamp", System.currentTimeMillis(),
                "productId", id
        ));
    }

    @GetMapping("/search")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER') or hasRole('VISITOR')")
    public ResponseEntity<List<ProductSummaryDTO>> searchProductsByName(
            @RequestParam String name) {

        if (name == null || name.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        List<ProductSummaryDTO> products = productService.searchProductsByName(name.trim());
        return ResponseEntity.ok(products);
    }

    @GetMapping("/category/{category}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER') or hasRole('VISITOR')")
    public ResponseEntity<List<ProductSummaryDTO>> getProductsByCategory(@PathVariable String category) {
        List<ProductSummaryDTO> products = productService.getProductsByCategory(category);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/low-stock")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER') or hasRole('VISITOR')")
    public ResponseEntity<List<ProductResponseDTO>> getProductsWithLowStock() {
        List<ProductResponseDTO> products = productService.getProductsWithLowStock();
        return ResponseEntity.ok(products);
    }

    @GetMapping("/out-of-stock")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER') or hasRole('VISITOR')")
    public ResponseEntity<List<ProductResponseDTO>> getProductsOutOfStock() {
        List<ProductResponseDTO> products = productService.getProductsOutOfStock();
        return ResponseEntity.ok(products);
    }

    @PatchMapping("/{id}/stock")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductResponseDTO> updateProductStock(@PathVariable Long id,
                                                                 @Valid @RequestBody StockUpdateDTO stockUpdateDTO) {
        ProductResponseDTO updatedProduct = productService.updateProductStock(id, stockUpdateDTO);
        return ResponseEntity.ok(updatedProduct);
    }

    @GetMapping("/categories")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER') or hasRole('VISITOR')")
    public ResponseEntity<List<String>> getAllCategories() {
        List<String> categories = productService.getAllCategories();
        return ResponseEntity.ok(categories);
    }

    @GetMapping("/price-range")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER') or hasRole('VISITOR')")
    public ResponseEntity<List<ProductSummaryDTO>> getProductsByPriceRange(
            @RequestParam
            @DecimalMin(value = "0.0", message = "El precio mínimo debe ser mayor o igual a cero")
            Double minPrice,
            @RequestParam
            @DecimalMin(value = "0.0", message = "El precio máximo debe ser mayor o igual a cero")
            Double maxPrice) {

        if (minPrice > maxPrice) {
            return ResponseEntity.badRequest()
                    .header("Error-Message", "El precio mínimo no puede ser mayor que el precio máximo")
                    .build();
        }

        List<ProductSummaryDTO> products = productService.getProductsByPriceRange(minPrice, maxPrice);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/stats")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER') or hasRole('VISITOR')")
    public ResponseEntity<Map<String, Object>> getProductStats() {
        Map<String, Object> stats = productService.getProductStats();
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/filters")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER') or hasRole('VISITOR')")
    public ResponseEntity<Page<ProductSummaryDTO>> getProductsWithFilters(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String name) {

        if (page < 0) page = 0;
        if (size <= 0 || size > 100) size = 10;

        Pageable pageable = PageRequest.of(page, size,
                sortDir.equalsIgnoreCase("desc") ?
                        Sort.by(sortBy).descending() :
                        Sort.by(sortBy).ascending());

        Page<ProductSummaryDTO> products = productService.getProductsWithFilters(category, name, pageable);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/count")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER') or hasRole('VISITOR')")
    public ResponseEntity<Map<String, Long>> getProductCounts() {
        Map<String, Object> stats = productService.getProductStats();

        Map<String, Long> counts = Map.of(
                "total", ((Integer) stats.get("totalProducts")).longValue(),
                "lowStock", ((Integer) stats.get("lowStockProducts")).longValue(),
                "outOfStock", ((Integer) stats.get("outOfStockProducts")).longValue(),
                "inStock", ((Integer) stats.get("inStockProducts")).longValue()
        );

        return ResponseEntity.ok(counts);
    }

    @GetMapping("/health")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER') or hasRole('VISITOR')")
    public ResponseEntity<Map<String, String>> healthCheck() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", "ProductService",
                "timestamp", String.valueOf(System.currentTimeMillis())
        ));
    }
}