package com.sistema_de_inventarios_v02;

import com.sistema_de_inventarios_v02.dto.*;
import com.sistema_de_inventarios_v02.exception.DuplicateProductException;
import com.sistema_de_inventarios_v02.exception.ProductNotFoundException;
import com.sistema_de_inventarios_v02.model.Product;
import com.sistema_de_inventarios_v02.repository.ProductRepository;
import com.sistema_de_inventarios_v02.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("ProductService Tests")
public class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    private Product testProduct;
    private CreateProductDTO createProductDTO;
    private UpdateProductDTO updateProductDTO;
    private StockUpdateDTO stockUpdateDTO;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Producto que simularía uno guardado en la base de datos
        testProduct = new Product(
                "Laptop",
                "Laptop de alta gama",
                "Electrónicos",
                new BigDecimal("1200.00"),
                10,
                5
        );
        testProduct.setId(1L);

        // DTO para crear producto (sin ID)
        createProductDTO = new CreateProductDTO(
                "Laptop",
                "Laptop de alta gama",
                "Electrónicos",
                new BigDecimal("1200.00"),
                10,
                5
        );

        // DTO para actualizar producto
        updateProductDTO = new UpdateProductDTO();
        updateProductDTO.setName("Laptop Actualizada");
        updateProductDTO.setDescription("Laptop de alta gama - Actualizada");
        updateProductDTO.setPrice(new BigDecimal("1300.00"));
        updateProductDTO.setStock(15);

        // DTO para actualizar stock
        stockUpdateDTO = new StockUpdateDTO(20, "Reposición de inventario");
    }

    @Nested
    @DisplayName("Crear Producto")
    class CreateProductTests {

        @Test
        @DisplayName("Debe crear producto exitosamente cuando el nombre no existe")
        void createProduct_ShouldReturnSavedProduct_WhenProductIsNew() {
            // Arrange
            when(productRepository.existsByNameIgnoreCase(createProductDTO.getName())).thenReturn(false);
            when(productRepository.save(any(Product.class))).thenReturn(testProduct);

            // Act
            ProductResponseDTO result = productService.createProduct(createProductDTO);

            // Assert
            assertNotNull(result);
            assertEquals(testProduct.getId(), result.getId());
            assertEquals(testProduct.getName(), result.getName());
            assertEquals(testProduct.getCategory(), result.getCategory());
            assertEquals(testProduct.getPrice(), result.getPrice());
            assertEquals(testProduct.getStock(), result.getStock());
            assertEquals(testProduct.getMinimumStock(), result.getMinimumStock());
            assertNotNull(result.getStockStatus());

            verify(productRepository, times(1)).existsByNameIgnoreCase(createProductDTO.getName());
            verify(productRepository, times(1)).save(any(Product.class));
        }

        @Test
        @DisplayName("Debe lanzar excepción cuando el nombre del producto ya existe")
        void createProduct_ShouldThrowException_WhenProductNameExists() {
            // Arrange
            when(productRepository.existsByNameIgnoreCase(createProductDTO.getName())).thenReturn(true);

            // Act & Assert
            assertThrows(DuplicateProductException.class, () -> {
                productService.createProduct(createProductDTO);
            });

            verify(productRepository, times(1)).existsByNameIgnoreCase(createProductDTO.getName());
            verify(productRepository, never()).save(any(Product.class));
        }
    }

    @Nested
    @DisplayName("Obtener Productos")
    class GetProductsTests {

        @Test
        @DisplayName("Debe retornar lista de ProductSummaryDTO cuando existen productos")
        void getAllProductsSummary_ShouldReturnListOfProductSummary() {
            // Arrange
            Product product1 = new Product("Laptop", "Laptop de alta gama", "Electrónicos",
                    new BigDecimal("1200.00"), 10, 5);
            product1.setId(1L);

            Product product2 = new Product("Mouse", "Mouse inalámbrico", "Electrónicos",
                    new BigDecimal("25.00"), 50, 10);
            product2.setId(2L);

            when(productRepository.findAll()).thenReturn(List.of(product1, product2));

            // Act
            List<ProductSummaryDTO> result = productService.getAllProductsSummary();

            // Assert
            assertNotNull(result);
            assertEquals(2, result.size());

            ProductSummaryDTO firstProduct = result.get(0);
            assertEquals(1L, firstProduct.getId());
            assertEquals("Laptop", firstProduct.getName());
            assertEquals("Electrónicos", firstProduct.getCategory());
            assertEquals(new BigDecimal("1200.00"), firstProduct.getPrice());
            assertEquals(10, firstProduct.getStock());
            assertEquals("IN_STOCK", firstProduct.getStockStatus());

            verify(productRepository, times(1)).findAll();
        }

        @Test
        @DisplayName("Debe retornar lista vacía cuando no existen productos")
        void getAllProductsSummary_ShouldReturnEmptyList_WhenNoProductsExist() {
            // Arrange
            when(productRepository.findAll()).thenReturn(List.of());

            // Act
            List<ProductSummaryDTO> result = productService.getAllProductsSummary();

            // Assert
            assertNotNull(result);
            assertTrue(result.isEmpty());
            verify(productRepository, times(1)).findAll();
        }

        @Test
        @DisplayName("Debe retornar ProductResponseDTO completo cuando busca por ID")
        void getProductById_ShouldReturnProductResponseDTO_WhenProductExists() {
            // Arrange
            when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));

            // Act
            ProductResponseDTO result = productService.getProductById(1L);

            // Assert
            assertNotNull(result);
            assertEquals(testProduct.getId(), result.getId());
            assertEquals(testProduct.getName(), result.getName());
            assertEquals(testProduct.getDescription(), result.getDescription());
            assertEquals(testProduct.getCategory(), result.getCategory());
            assertEquals(testProduct.getPrice(), result.getPrice());
            assertEquals(testProduct.getStock(), result.getStock());
            assertEquals(testProduct.getMinimumStock(), result.getMinimumStock());
            assertEquals(testProduct.isLowStock(), result.isLowStock());
            assertEquals(testProduct.isOutOfStock(), result.isOutOfStock());
            assertNotNull(result.getStockStatus());

            verify(productRepository, times(1)).findById(1L);
        }

        @Test
        @DisplayName("Debe lanzar excepción cuando el producto no existe")
        void getProductById_ShouldThrowException_WhenProductDoesNotExist() {
            // Arrange
            when(productRepository.findById(1L)).thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(ProductNotFoundException.class, () -> {
                productService.getProductById(1L);
            });

            verify(productRepository, times(1)).findById(1L);
        }
    }

    @Nested
    @DisplayName("Actualizar Producto")
    class UpdateProductTests {

        @Test
        @DisplayName("Debe actualizar producto exitosamente con UpdateProductDTO")
        void updateProduct_ShouldReturnUpdatedProduct_WhenProductExists() {
            // Arrange
            when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
            when(productRepository.existsByNameIgnoreCase("Laptop Actualizada")).thenReturn(false);
            when(productRepository.save(any(Product.class))).thenReturn(testProduct);

            // Act
            ProductResponseDTO result = productService.updateProduct(1L, updateProductDTO);

            // Assert
            assertNotNull(result);
            verify(productRepository, times(1)).findById(1L);
            verify(productRepository, times(1)).save(any(Product.class));
        }

        @Test
        @DisplayName("Debe lanzar excepción cuando el producto a actualizar no existe")
        void updateProduct_ShouldThrowException_WhenProductDoesNotExist() {
            // Arrange
            when(productRepository.findById(1L)).thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(ProductNotFoundException.class, () -> {
                productService.updateProduct(1L, updateProductDTO);
            });

            verify(productRepository, times(1)).findById(1L);
            verify(productRepository, never()).save(any(Product.class));
        }

        @Test
        @DisplayName("Debe actualizar solo el stock con StockUpdateDTO")
        void updateProductStock_ShouldReturnUpdatedProduct_WhenProductExists() {
            // Arrange
            when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
            when(productRepository.save(any(Product.class))).thenReturn(testProduct);

            // Act
            ProductResponseDTO result = productService.updateProductStock(1L, stockUpdateDTO);

            // Assert
            assertNotNull(result);
            verify(productRepository, times(1)).findById(1L);
            verify(productRepository, times(1)).save(any(Product.class));
        }
    }

    @Nested
    @DisplayName("Eliminar Producto")
    class DeleteProductTests {

        @Test
        @DisplayName("Debe eliminar producto exitosamente cuando existe")
        void deleteProduct_ShouldDeleteProduct_WhenProductExists() {
            // Arrange
            when(productRepository.existsById(1L)).thenReturn(true);

            // Act
            productService.deleteProduct(1L);

            // Assert
            verify(productRepository, times(1)).existsById(1L);
            verify(productRepository, times(1)).deleteById(1L);
        }

        @Test
        @DisplayName("Debe lanzar excepción cuando el producto no existe")
        void deleteProduct_ShouldThrowException_WhenProductDoesNotExist() {
            // Arrange
            when(productRepository.existsById(1L)).thenReturn(false);

            // Act & Assert
            assertThrows(ProductNotFoundException.class, () -> {
                productService.deleteProduct(1L);
            });

            verify(productRepository, times(1)).existsById(1L);
            verify(productRepository, never()).deleteById(anyLong());
        }
    }

    @Nested
    @DisplayName("Búsquedas")
    class SearchTests {

        @Test
        @DisplayName("Debe buscar productos por nombre y retornar ProductSummaryDTO")
        void searchProductsByName_ShouldReturnListOfProductSummary_WhenNameMatches() {
            // Arrange
            when(productRepository.findByNameContainingIgnoreCase("Laptop")).thenReturn(List.of(testProduct));

            // Act
            List<ProductSummaryDTO> result = productService.searchProductsByName("Laptop");

            // Assert
            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals(testProduct.getId(), result.get(0).getId());
            assertEquals(testProduct.getName(), result.get(0).getName());

            verify(productRepository, times(1)).findByNameContainingIgnoreCase("Laptop");
        }

        @Test
        @DisplayName("Debe buscar productos por categoría y retornar ProductSummaryDTO")
        void getProductsByCategory_ShouldReturnListOfProductSummary_WhenCategoryMatches() {
            // Arrange
            when(productRepository.findByCategoryIgnoreCase("Electrónicos")).thenReturn(List.of(testProduct));

            // Act
            List<ProductSummaryDTO> result = productService.getProductsByCategory("Electrónicos");

            // Assert
            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals(testProduct.getCategory(), result.get(0).getCategory());

            verify(productRepository, times(1)).findByCategoryIgnoreCase("Electrónicos");
        }

        @Test
        @DisplayName("Debe buscar productos por rango de precio y retornar ProductSummaryDTO")
        void getProductsByPriceRange_ShouldReturnListOfProductSummary_WhenPriceRangeMatches() {
            // Arrange
            when(productRepository.findByPriceBetween(1000.00, 1500.00)).thenReturn(List.of(testProduct));

            // Act
            List<ProductSummaryDTO> result = productService.getProductsByPriceRange(1000.00, 1500.00);

            // Assert
            assertNotNull(result);
            assertEquals(1, result.size());
            assertTrue(result.get(0).getPrice().doubleValue() >= 1000.00);
            assertTrue(result.get(0).getPrice().doubleValue() <= 1500.00);

            verify(productRepository, times(1)).findByPriceBetween(1000.00, 1500.00);
        }
    }

    @Nested
    @DisplayName("Gestión de Stock")
    class StockManagementTests {

        @Test
        @DisplayName("Debe obtener productos con stock bajo como ProductResponseDTO")
        void getProductsWithLowStock_ShouldReturnListOfProductResponse_WhenLowStockExists() {
            // Arrange
            Product lowStockProduct = new Product("Producto Bajo", "Descripción", "Categoría",
                    new BigDecimal("100.00"), 2, 5);
            lowStockProduct.setId(2L);

            when(productRepository.findProductsWithLowStock()).thenReturn(List.of(lowStockProduct));

            // Act
            List<ProductResponseDTO> result = productService.getProductsWithLowStock();

            // Assert
            assertNotNull(result);
            assertEquals(1, result.size());
            assertTrue(result.get(0).isLowStock());
            assertEquals("LOW_STOCK", result.get(0).getStockStatus());

            verify(productRepository, times(1)).findProductsWithLowStock();
        }

        @Test
        @DisplayName("Debe obtener productos sin stock como ProductResponseDTO")
        void getProductsOutOfStock_ShouldReturnListOfProductResponse_WhenOutOfStockExists() {
            // Arrange
            Product outOfStockProduct = new Product("Producto Agotado", "Descripción", "Categoría",
                    new BigDecimal("100.00"), 0, 5);
            outOfStockProduct.setId(3L);

            when(productRepository.findProductsOutOfStock()).thenReturn(List.of(outOfStockProduct));

            // Act
            List<ProductResponseDTO> result = productService.getProductsOutOfStock();

            // Assert
            assertNotNull(result);
            assertEquals(1, result.size());
            assertTrue(result.get(0).isOutOfStock());
            assertEquals("OUT_OF_STOCK", result.get(0).getStockStatus());

            verify(productRepository, times(1)).findProductsOutOfStock();
        }
    }

    @Nested
    @DisplayName("Paginación")
    class PaginationTests {

        @Test
        @DisplayName("Debe retornar productos paginados como ProductSummaryDTO")
        void getAllProductsPaginated_ShouldReturnPaginatedProductSummary_WhenProductsExist() {
            // Arrange
            Product product1 = new Product("Laptop", "Descripción", "Electrónicos",
                    new BigDecimal("1200.00"), 10, 5);
            product1.setId(1L);

            Product product2 = new Product("Mouse", "Descripción", "Electrónicos",
                    new BigDecimal("25.00"), 50, 10);
            product2.setId(2L);

            Page<Product> mockPage = new PageImpl<>(List.of(product1, product2));
            Pageable pageable = PageRequest.of(0, 10);

            when(productRepository.findAll(pageable)).thenReturn(mockPage);

            // Act
            Page<ProductSummaryDTO> result = productService.getAllProductsPaginated(pageable);

            // Assert
            assertNotNull(result);
            assertEquals(2, result.getContent().size());

            ProductSummaryDTO firstProduct = result.getContent().get(0);
            assertEquals(1L, firstProduct.getId());
            assertEquals("Laptop", firstProduct.getName());
            assertEquals("Electrónicos", firstProduct.getCategory());

            verify(productRepository, times(1)).findAll(pageable);
        }

        @Test
        @DisplayName("Debe buscar con filtros y paginación como ProductSummaryDTO")
        void getProductsWithFilters_ShouldReturnFilteredAndPagedProductSummary() {
            // Arrange
            Page<Product> mockPage = new PageImpl<>(List.of(testProduct));
            Pageable pageable = PageRequest.of(0, 10);

            when(productRepository.findByCategoryAndName("Electrónicos", "Laptop", pageable))
                    .thenReturn(mockPage);

            // Act
            Page<ProductSummaryDTO> result = productService.getProductsWithFilters("Electrónicos", "Laptop", pageable);

            // Assert
            assertNotNull(result);
            assertEquals(1, result.getContent().size());
            assertEquals("Electrónicos", result.getContent().get(0).getCategory());

            verify(productRepository, times(1)).findByCategoryAndName("Electrónicos", "Laptop", pageable);
        }
    }

    @Nested
    @DisplayName("Utilidades")
    class UtilityTests {

        @Test
        @DisplayName("Debe obtener todas las categorías")
        void getAllCategories_ShouldReturnListOfCategories_WhenCategoriesExist() {
            // Arrange
            when(productRepository.findAllCategories()).thenReturn(List.of("Electrónicos", "Hogar", "Jardín"));

            // Act
            List<String> result = productService.getAllCategories();

            // Assert
            assertNotNull(result);
            assertEquals(3, result.size());
            assertTrue(result.contains("Electrónicos"));
            assertTrue(result.contains("Hogar"));
            assertTrue(result.contains("Jardín"));

            verify(productRepository, times(1)).findAllCategories();
        }

        @Test
        @DisplayName("Debe obtener estadísticas de productos")
        void getProductStats_ShouldReturnCorrectStatistics() {
            // Arrange
            when(productRepository.findAll()).thenReturn(List.of(testProduct));
            when(productRepository.findProductsWithLowStock()).thenReturn(List.of());
            when(productRepository.findProductsOutOfStock()).thenReturn(List.of());
            when(productRepository.findAllCategories()).thenReturn(List.of("Electrónicos"));

            // Act
            Map<String, Object> result = productService.getProductStats();

            // Assert
            assertNotNull(result);
            assertEquals(1, result.get("totalProducts"));
            assertEquals(0, result.get("lowStockProducts"));
            assertEquals(0, result.get("outOfStockProducts"));
            assertEquals(1, result.get("totalCategories"));
            assertEquals(1, result.get("inStockProducts"));

            verify(productRepository, times(1)).findAll();
            verify(productRepository, times(1)).findProductsWithLowStock();
            verify(productRepository, times(1)).findProductsOutOfStock();
            verify(productRepository, times(1)).findAllCategories();
        }
    }
}