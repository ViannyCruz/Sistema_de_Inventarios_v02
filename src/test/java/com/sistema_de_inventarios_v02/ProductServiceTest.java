package com.sistema_de_inventarios_v02;

import com.sistema_de_inventarios_v02.dto.ProductDTO;
import com.sistema_de_inventarios_v02.exception.DuplicateProductException;
import com.sistema_de_inventarios_v02.exception.ProductNotFoundException;
import com.sistema_de_inventarios_v02.model.Product;
import com.sistema_de_inventarios_v02.repository.ProductRepository;
import com.sistema_de_inventarios_v02.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ProductServiceTest {
    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    private Product testProduct;
    private ProductDTO testProductDTO;
    private ProductDTO testProductInputDTO;

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

        // DTO que simularía un producto existente
        testProductDTO = new ProductDTO(
                1L,
                "Laptop",
                "Laptop de alta gama",
                "Electrónicos",
                new BigDecimal("1200.00"),
                10,
                5
        );

        // DTO para simular entrada al crear un producto (sin ID)
        testProductInputDTO = new ProductDTO(
                null,
                "Laptop",
                "Laptop de alta gama",
                "Electrónicos",
                new BigDecimal("1200.00"),
                10,
                5
        );
    }

    @Test
    void createProduct_ShouldReturnSavedProduct_WhenProductIsNew() {
        // Configuración del mock
        when(productRepository.existsByNameIgnoreCase(testProductInputDTO.getName())).thenReturn(false);
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);

        // Ejecución
        ProductDTO result = productService.createProduct(testProductInputDTO);

        // Verificaciones
        assertNotNull(result);
        assertEquals(testProductDTO.getId(), result.getId());
        assertEquals(testProductDTO.getName(), result.getName());
        assertEquals(testProductDTO.getCategory(), result.getCategory());

        verify(productRepository, times(1)).existsByNameIgnoreCase(testProductInputDTO.getName());
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    void createProduct_ShouldThrowException_WhenProductNameExists() {
        // Configuración del mock
        when(productRepository.existsByNameIgnoreCase(testProductInputDTO.getName())).thenReturn(true);

        // Ejecución y verificación de excepción
        assertThrows(DuplicateProductException.class, () -> {
            productService.createProduct(testProductInputDTO);
        });

        verify(productRepository, times(1)).existsByNameIgnoreCase(testProductInputDTO.getName());
        verify(productRepository, never()).save(any(Product.class));
    }




    @Test
    void getAllProducts_ShouldReturnListOfProducts() {
        Product product1 = new Product(
                "Laptop",
                "Laptop de alta gama",
                "Electrónicos",
                new BigDecimal("1200.00"),
                10,
                5
        );
        product1.setId(1L);

        Product product2 = new Product(
                "Mouse",
                "Mouse inalámbrico",
                "Electrónicos",
                new BigDecimal("25.00"),
                50,
                10
        );
        product2.setId(2L);

        List<Product> mockProducts = List.of(product1, product2);

        // Configurar el mock
        when(productRepository.findAll()).thenReturn(mockProducts);

        // Ejecutar el metodo a probar
        List<ProductDTO> result = productService.getAllProducts();

        // Verificar los resultados
        assertNotNull(result);
        assertEquals(2, result.size());

        // Verificar el primer producto
        ProductDTO firstProduct = result.getFirst();
        assertEquals(1L, firstProduct.getId());
        assertEquals("Laptop", firstProduct.getName());
        assertEquals("Laptop de alta gama", firstProduct.getDescription());
        assertEquals("Electrónicos", firstProduct.getCategory());
        assertEquals(new BigDecimal("1200.00"), firstProduct.getPrice());
        assertEquals(10, firstProduct.getStock());
        assertEquals(5, firstProduct.getMinimumStock());

        // Verificar el segundo producto
        ProductDTO secondProduct = result.get(1);
        assertEquals(2L, secondProduct.getId());
        assertEquals("Mouse", secondProduct.getName());
        assertEquals("Mouse inalámbrico", secondProduct.getDescription());
        assertEquals("Electrónicos", secondProduct.getCategory());
        assertEquals(new BigDecimal("25.00"), secondProduct.getPrice());
        assertEquals(50, secondProduct.getStock());
        assertEquals(10, secondProduct.getMinimumStock());

        // Verificar que el repositorio fue llamado correctamente
        verify(productRepository, times(1)).findAll();
    }

    @Test
    void getAllProducts_ShouldReturnEmptyList_WhenNoProductsExist() {
        // Arrange
        when(productRepository.findAll()).thenReturn(List.of());

        // Act
        List<ProductDTO> result = productService.getAllProducts();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        assertEquals(0, result.size());

        // Verify
        verify(productRepository, times(1)).findAll();
    }

/*
    @Test
    void getProductsWithFilters_ShouldReturnFilteredProducts_WhenFiltersAreApplied() {
        // Configurar el mock
        when(productRepository.findByCategoryAndName("Electrónicos", "Laptop", Mockito.any()))
                .thenReturn((Page<Product>) List.of(testProduct));

        // Ejecutar el metodo a probar
        List<ProductDTO> result = productService.getProductsWithFilters("Electrónicos", "Laptop", null)
                .stream()
                .collect(Collectors.toList());

        // Verificar los resultados
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testProductDTO.getId(), result.get(0).getId());
        assertEquals(testProductDTO.getName(), result.get(0).getName());
        assertEquals(testProductDTO.getDescription(), result.get(0).getDescription());
        assertEquals(testProductDTO.getCategory(), result.get(0).getCategory());
        assertEquals(testProductDTO.getPrice(), result.get(0).getPrice());
        assertEquals(testProductDTO.getStock(), result.get(0).getStock());
        assertEquals(testProductDTO.getMinimumStock(), result.get(0).getMinimumStock());

        // Verificar que el repositorio fue llamado correctamente
        verify(productRepository, times(1)).findByCategoryAndName("Electrónicos", "Laptop", Mockito.any());
    }


*/
    @Test
    void getProductById_ShouldReturnProduct_WhenProductExists() {
        // Configurar el mock
        when(productRepository.findById(1L)).thenReturn(java.util.Optional.of(testProduct));

        // Ejecutar el metodo a probar
        ProductDTO result = productService.getProductById(1L);

        // Verificar los resultados
        assertNotNull(result);
        assertEquals(testProductDTO.getId(), result.getId());
        assertEquals(testProductDTO.getName(), result.getName());
        assertEquals(testProductDTO.getDescription(), result.getDescription());
        assertEquals(testProductDTO.getCategory(), result.getCategory());
        assertEquals(testProductDTO.getPrice(), result.getPrice());
        assertEquals(testProductDTO.getStock(), result.getStock());
        assertEquals(testProductDTO.getMinimumStock(), result.getMinimumStock());

        // Verificar que el repositorio fue llamado correctamente
        verify(productRepository, times(1)).findById(1L);
    }
    
    /*
    @Test
    void updateProduct_ShouldReturnUpdatedProduct_WhenProductExists() {
        // Configurar el mock
        when(productRepository.findById(1L)).thenReturn(java.util.Optional.of(testProduct));
        when(productRepository.existsByNameIgnoreCase("Laptop")).thenReturn(false);
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);

        // Crear un DTO de producto actualizado
        ProductDTO updatedProductDTO = new ProductDTO(
                1L,
                "Laptop",
                "Laptop de alta gama - Actualizado",
                "Electrónicos",
                new BigDecimal("1300.00"),
                15,
                5
        );

        // Ejecutar el metodo a probar
        ProductDTO result = productService.updateProduct(1L, updatedProductDTO);

        // Verificar los resultados
        assertNotNull(result);
        assertEquals(updatedProductDTO.getId(), result.getId());
        assertEquals(updatedProductDTO.getName(), result.getName());
        assertEquals(updatedProductDTO.getDescription(), result.getDescription());
        assertEquals(updatedProductDTO.getCategory(), result.getCategory());
        assertEquals(updatedProductDTO.getPrice(), result.getPrice());
        assertEquals(updatedProductDTO.getStock(), result.getStock());
        assertEquals(updatedProductDTO.getMinimumStock(), result.getMinimumStock());

        // Verificar que el repositorio fue llamado correctamente
        verify(productRepository, times(1)).findById(1L);
        verify(productRepository, times(1)).existsByNameIgnoreCase("Laptop");
        verify(productRepository, times(1)).save(any(Product.class));
    }

*/

    @Test
    void updateProduct_ShouldThrowException_WhenProductDoesNotExist() {
        // Configurar el mock
        when(productRepository.findById(1L)).thenReturn(java.util.Optional.empty());

        // Crear un DTO de producto actualizado
        ProductDTO updatedProductDTO = new ProductDTO(
                1L,
                "Laptop",
                "Laptop de alta gama - Actualizado",
                "Electrónicos",
                new BigDecimal("1300.00"),
                15,
                5
        );

        // Ejecutar y verificar la excepción
        assertThrows(ProductNotFoundException.class, () -> {
            productService.updateProduct(1L, updatedProductDTO);
        });

        // Verificar que el repositorio fue llamado correctamente
        verify(productRepository, times(1)).findById(1L);
        verify(productRepository, never()).existsByNameIgnoreCase(anyString());
        verify(productRepository, never()).save(any(Product.class));
    }

/*
    @Test
    void updateProduct_ShouldThrowException_WhenProductNameExists() {
        // Configurar el mock
        when(productRepository.findById(1L)).thenReturn(java.util.Optional.of(testProduct));
        when(productRepository.existsByNameIgnoreCase("Laptop")).thenReturn(true);

        // Crear un DTO de producto actualizado
        ProductDTO updatedProductDTO = new ProductDTO(
                1L,
                "Laptop",
                "Laptop de alta gama - Actualizado",
                "Electrónicos",
                new BigDecimal("1300.00"),
                15,
                5
        );

        // Ejecutar y verificar la excepción
        assertThrows(DuplicateProductException.class, () -> {
            productService.updateProduct(1L, updatedProductDTO);
        });

        // Verificar que el repositorio fue llamado correctamente
        verify(productRepository, times(1)).findById(1L);
        verify(productRepository, times(1)).existsByNameIgnoreCase("Laptop");
        verify(productRepository, never()).save(any(Product.class));
    }*/

    /*
    @Test
    void deleteProduct_ShouldDeleteProduct_WhenProductExists() {
        // Configurar el mock
        when(productRepository.findById(1L)).thenReturn(java.util.Optional.of(testProduct));

        // Ejecutar el metodo a probar
        productService.deleteProduct(1L);

        // Verificar que el producto fue eliminado
        verify(productRepository, times(1)).deleteById(1L);
    }*/


    @Test
    void deleteProduct_ShouldThrowException_WhenProductDoesNotExist() {
        // Configurar el mock
        when(productRepository.findById(1L)).thenReturn(java.util.Optional.empty());

        // Ejecutar y verificar la excepción
        assertThrows(ProductNotFoundException.class, () -> {
            productService.deleteProduct(1L);
        });

        // Verificar que el repositorio no intentó eliminar nada
        verify(productRepository, never()).deleteById(anyLong());
    }


    @Test
    void searchProductsByName_ShouldReturnListOfProducts_WhenNameMatches() {
        // Configurar el mock
        when(productRepository.findByNameContainingIgnoreCase("Laptop")).thenReturn(List.of(testProduct));

        // Ejecutar el metodo a probar
        List<ProductDTO> result = productService.searchProductsByName("Laptop");

        // Verificar los resultados
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testProductDTO.getId(), result.get(0).getId());
        assertEquals(testProductDTO.getName(), result.get(0).getName());

        // Verificar que el repositorio fue llamado correctamente
        verify(productRepository, times(1)).findByNameContainingIgnoreCase("Laptop");
    }

    @Test
    void searchProductsByName_ShouldReturnEmptyList_WhenNoProductsMatch() {
        // Configurar el mock
        when(productRepository.findByNameContainingIgnoreCase("NonExistent")).thenReturn(List.of());

        // Ejecutar el metodo a probar
        List<ProductDTO> result = productService.searchProductsByName("NonExistent");

        // Verificar los resultados
        assertNotNull(result);
        assertTrue(result.isEmpty());

        // Verificar que el repositorio fue llamado correctamente
        verify(productRepository, times(1)).findByNameContainingIgnoreCase("NonExistent");
    }

    @Test
    void getProductsByCategory_ShouldReturnListOfProducts_WhenCategoryMatches() {
        // Configurar el mock
        when(productRepository.findByCategoryIgnoreCase("Electrónicos")).thenReturn(List.of(testProduct));

        // Ejecutar el metodo a probar
        List<ProductDTO> result = productService.getProductsByCategory("Electrónicos");

        // Verificar los resultados
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testProductDTO.getId(), result.get(0).getId());
        assertEquals(testProductDTO.getName(), result.get(0).getName());

        // Verificar que el repositorio fue llamado correctamente
        verify(productRepository, times(1)).findByCategoryIgnoreCase("Electrónicos");
    }

    @Test
    void getProductsByCategory_ShouldReturnEmptyList_WhenNoProductsMatch() {
        // Configurar el mock
        when(productRepository.findByCategoryIgnoreCase("NonExistent")).thenReturn(List.of());

        // Ejecutar el metodo a probar
        List<ProductDTO> result = productService.getProductsByCategory("NonExistent");

        // Verificar los resultados
        assertNotNull(result);
        assertTrue(result.isEmpty());

        // Verificar que el repositorio fue llamado correctamente
        verify(productRepository, times(1)).findByCategoryIgnoreCase("NonExistent");
    }


    @Test
    void getProductsWithLowStock_ShouldReturnListOfProducts_WhenLowStockExists() {
        // Configurar el mock
        when(productRepository.findProductsWithLowStock()).thenReturn(List.of(testProduct));

        // Ejecutar el metodo a probar
        List<ProductDTO> result = productService.getProductsWithLowStock();

        // Verificar los resultados
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testProductDTO.getId(), result.get(0).getId());
        assertEquals(testProductDTO.getName(), result.get(0).getName());

        // Verificar que el repositorio fue llamado correctamente
        verify(productRepository, times(1)).findProductsWithLowStock();
    }

    @Test
    void getProductsWithLowStock_ShouldReturnEmptyList_WhenNoLowStockExists() {
        // Configurar el mock
        when(productRepository.findProductsWithLowStock()).thenReturn(List.of());

        // Ejecutar el metodo a probar
        List<ProductDTO> result = productService.getProductsWithLowStock();

        // Verificar los resultados
        assertNotNull(result);
        assertTrue(result.isEmpty());

        // Verificar que el repositorio fue llamado correctamente
        verify(productRepository, times(1)).findProductsWithLowStock();
    }

    @Test
    void getProductsOutOfStock_ShouldReturnListOfProducts_WhenOutOfStockExists() {
        // Configurar el mock
        when(productRepository.findProductsOutOfStock()).thenReturn(List.of(testProduct));

        // Ejecutar el metodo a probar
        List<ProductDTO> result = productService.getProductsOutOfStock();

        // Verificar los resultados
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testProductDTO.getId(), result.get(0).getId());
        assertEquals(testProductDTO.getName(), result.get(0).getName());

        // Verificar que el repositorio fue llamado correctamente
        verify(productRepository, times(1)).findProductsOutOfStock();
    }

    @Test
    void getProductsOutOfStock_ShouldReturnEmptyList_WhenNoOutOfStockExists() {
        // Configurar el mock
        when(productRepository.findProductsOutOfStock()).thenReturn(List.of());

        // Ejecutar el metodo a probar
        List<ProductDTO> result = productService.getProductsOutOfStock();

        // Verificar los resultados
        assertNotNull(result);
        assertTrue(result.isEmpty());

        // Verificar que el repositorio fue llamado correctamente
        verify(productRepository, times(1)).findProductsOutOfStock();
    }

    @Test
    void getProductsByPriceRange_ShouldReturnListOfProducts_WhenPriceRangeMatches() {
        // Configurar el mock
        when(productRepository.findByPriceBetween(1000.00, 1500.00)).thenReturn(List.of(testProduct));

        // Ejecutar el metodo a probar
        List<ProductDTO> result = productService.getProductsByPriceRange(1000.00, 1500.00);

        // Verificar los resultados
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testProductDTO.getId(), result.get(0).getId());
        assertEquals(testProductDTO.getName(), result.get(0).getName());

        // Verificar que el repositorio fue llamado correctamente
        verify(productRepository, times(1)).findByPriceBetween(1000.00, 1500.00);
    }

    @Test
    void getProductsByPriceRange_ShouldReturnEmptyList_WhenNoProductsMatchPriceRange() {
        // Configurar el mock
        when(productRepository.findByPriceBetween(2000.00, 3000.00)).thenReturn(List.of());

        // Ejecutar el metodo a probar
        List<ProductDTO> result = productService.getProductsByPriceRange(2000.00, 3000.00);

        // Verificar los resultados
        assertNotNull(result);
        assertTrue(result.isEmpty());

        // Verificar que el repositorio fue llamado correctamente
        verify(productRepository, times(1)).findByPriceBetween(2000.00, 3000.00);
    }

    @Test
    void getAllCategories_ShouldReturnListOfCategories_WhenCategoriesExist() {
        // Configurar el mock
        when(productRepository.findAllCategories()).thenReturn(List.of("Electrónicos", "Hogar", "Jardín"));

        // Ejecutar el metodo a probar
        List<String> result = productService.getAllCategories();

        // Verificar los resultados
        assertNotNull(result);
        assertEquals(3, result.size());
        assertTrue(result.contains("Electrónicos"));
        assertTrue(result.contains("Hogar"));
        assertTrue(result.contains("Jardín"));

        // Verificar que el repositorio fue llamado correctamente
        verify(productRepository, times(1)).findAllCategories();
    }

    @Test
    void getAllCategories_ShouldReturnEmptyList_WhenNoCategoriesExist() {
        // Configurar el mock
        when(productRepository.findAllCategories()).thenReturn(List.of());

        // Ejecutar el metodo a probar
        List<String> result = productService.getAllCategories();

        // Verificar los resultados
        assertNotNull(result);
        assertTrue(result.isEmpty());

        // Verificar que el repositorio fue llamado correctamente
        verify(productRepository, times(1)).findAllCategories();
    }

    @Test
    void getAllProductsPaginated_ShouldReturnPaginatedProducts_WhenProductsExist() {
        // Arrange
        Product product1 = new Product(
                "Laptop",
                "Laptop de alta gama",
                "Electrónicos",
                new BigDecimal("1200.00"),
                10,
                5
        );
        product1.setId(1L);

        Product product2 = new Product(
                "Mouse",
                "Mouse inalámbrico",
                "Electrónicos",
                new BigDecimal("25.00"),
                50,
                10
        );
        product2.setId(2L);

        Page<Product> mockPage = new PageImpl<>(List.of(product1, product2));
        Pageable pageable = PageRequest.of(0, 10);

        when(productRepository.findAll(pageable)).thenReturn(mockPage);

        // Act
        Page<ProductDTO> result = productService.getAllProductsPaginated(pageable);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.getContent().size());

        // Verify first product
        ProductDTO firstProduct = result.getContent().get(0);
        assertEquals(1L, firstProduct.getId());
        assertEquals("Laptop", firstProduct.getName());
        assertEquals("Laptop de alta gama", firstProduct.getDescription());
        assertEquals("Electrónicos", firstProduct.getCategory());
        assertEquals(new BigDecimal("1200.00"), firstProduct.getPrice());
        assertEquals(10, firstProduct.getStock());
        assertEquals(5, firstProduct.getMinimumStock());

        // Verify second product
        ProductDTO secondProduct = result.getContent().get(1);
        assertEquals(2L, secondProduct.getId());
        assertEquals("Mouse", secondProduct.getName());
        assertEquals("Mouse inalámbrico", secondProduct.getDescription());
        assertEquals("Electrónicos", secondProduct.getCategory());
        assertEquals(new BigDecimal("25.00"), secondProduct.getPrice());
        assertEquals(50, secondProduct.getStock());
        assertEquals(10, secondProduct.getMinimumStock());

        verify(productRepository, times(1)).findAll(pageable);
    }

    @Test
    void getAllProductsPaginated_ShouldReturnEmptyPage_WhenNoProductsExist() {
        Page<Product> mockPage = new PageImpl<>(List.of());
        Pageable pageable = PageRequest.of(0, 10);

        when(productRepository.findAll(pageable)).thenReturn(mockPage);

        Page<ProductDTO> result = productService.getAllProductsPaginated(pageable);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        assertEquals(0, result.getContent().size());

        verify(productRepository, times(1)).findAll(pageable);
    }

    @Test
    void getAllProductsPaginated_ShouldReturnCorrectPageInfo_WhenPagingParametersAreProvided() {
        List<Product> allProducts = List.of(
                new Product("Product1", "Desc1", "Cat1", BigDecimal.ONE, 1, 1),
                new Product("Product2", "Desc2", "Cat2", BigDecimal.TEN, 2, 2),
                new Product("Product3", "Desc3", "Cat3", new BigDecimal("100"), 3, 3)
        );

        Pageable pageable = PageRequest.of(1, 2);
        Page<Product> mockPage = new PageImpl<>(
                List.of(allProducts.get(2)),
                pageable,
                3
        );

        when(productRepository.findAll(pageable)).thenReturn(mockPage);

        Page<ProductDTO> result = productService.getAllProductsPaginated(pageable);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(3, result.getTotalElements());
        assertEquals(2, result.getTotalPages());
        assertEquals(1, result.getNumber());
        assertEquals(2, result.getSize());

        verify(productRepository, times(1)).findAll(pageable);
    }

















}