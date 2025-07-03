package com.sistema_de_inventarios_v02;

import com.sistema_de_inventarios_v02.model.Product;
import com.sistema_de_inventarios_v02.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("ProductRepository Tests")
class ProductRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ProductRepository productRepository;

    private Product laptop;
    private Product mouse;
    private Product keyboard;
    private Product monitor;
    private Product outOfStockProduct;

    @BeforeEach
    void setUp() {
        // Limpiar datos previos
        productRepository.deleteAll();
        entityManager.flush();

        // Crear productos de prueba con stock valido
        laptop = createProduct("Laptop Gaming", "Electronics", BigDecimal.valueOf(1500.0), 5, 3);
        mouse = createProduct("Mouse Inalámbrico", "Electronics", BigDecimal.valueOf(25.0), 15, 5);
        keyboard = createProduct("Teclado Mecánico", "Electronics", BigDecimal.valueOf(80.0), 2, 5); // Stock bajo
        monitor = createProduct("Monitor 4K", "Electronics", BigDecimal.valueOf(300.0), 0, 2); // Sin stock
        outOfStockProduct = createProduct("Producto Agotado", "Office", BigDecimal.valueOf(50.0), 0, 10); // Stock 0 en lugar de null

        // Persistir productos
        entityManager.persistAndFlush(laptop);
        entityManager.persistAndFlush(mouse);
        entityManager.persistAndFlush(keyboard);
        entityManager.persistAndFlush(monitor);
        entityManager.persistAndFlush(outOfStockProduct);
    }

    private Product createProduct(String name, String category, BigDecimal price, Integer stock, Integer minimumStock) {
        Product product = new Product();
        product.setName(name);
        product.setCategory(category);
        product.setPrice(price);
        product.setStock(stock);
        product.setMinimumStock(minimumStock);
        return product;
    }

    @Nested
    @DisplayName("Búsqueda por nombre")
    class FindByNameTests {

        @Test
        @DisplayName("Debe encontrar productos por nombre parcial ignorando mayúsculas")
        void findByNameContainingIgnoreCase_ShouldReturnMatchingProducts() {
            List<Product> results = productRepository.findByNameContainingIgnoreCase("laptop");

            assertThat(results).hasSize(1);
            assertThat(results.get(0).getName()).containsIgnoringCase("laptop");
        }

        @Test
        @DisplayName("Debe encontrar multiples productos con nombre similar")
        void findByNameContainingIgnoreCase_ShouldReturnMultipleMatches() {
            Product gamingMouse = createProduct("Gaming Mouse Pro", "Electronics", BigDecimal.valueOf(45.0), 8, 3);
            entityManager.persistAndFlush(gamingMouse);

            List<Product> results = productRepository.findByNameContainingIgnoreCase("mouse");

            assertThat(results).hasSize(2);
            assertThat(results).extracting(Product::getName)
                    .allMatch(name -> name.toLowerCase().contains("mouse"));
        }

        @Test
        @DisplayName("Debe retornar lista vacía cuando no encuentra coincidencias")
        void findByNameContainingIgnoreCase_ShouldReturnEmptyWhenNotFound() {
            List<Product> results = productRepository.findByNameContainingIgnoreCase("inexistente");

            assertThat(results).isEmpty();
        }

        @Test
        @DisplayName("Debe manejar busquedas con caracteres especiales")
        void findByNameContainingIgnoreCase_ShouldHandleSpecialCharacters() {
            Product specialProduct = createProduct("Producto-Especial_123", "Special", BigDecimal.valueOf(100.0), 5, 2);
            entityManager.persistAndFlush(specialProduct);

            List<Product> results = productRepository.findByNameContainingIgnoreCase("especial");

            assertThat(results).hasSize(1);
        }
    }

    @Nested
    @DisplayName("Busqueda por categoría")
    class FindByCategoryTests {

        @Test
        @DisplayName("Debe encontrar productos por categoría exacta ignorando mayusculas")
        void findByCategoryIgnoreCase_ShouldReturnProductsInCategory() {
            List<Product> results = productRepository.findByCategoryIgnoreCase("electronics");

            assertThat(results).hasSize(4);
            assertThat(results).extracting(Product::getCategory)
                    .allMatch(category -> category.equalsIgnoreCase("electronics"));
        }

        @Test
        @DisplayName("Debe retornar lista vacía para categoría inexistente")
        void findByCategoryIgnoreCase_ShouldReturnEmptyForNonExistentCategory() {
            List<Product> results = productRepository.findByCategoryIgnoreCase("inexistente");

            assertThat(results).isEmpty();
        }
    }

    @Nested
    @DisplayName("Gestion de stock")
    class StockManagementTests {

        @Test
        @DisplayName("Debe encontrar productos con stock bajo (stock > 0 pero <= mínimo)")
        void findProductsWithLowStock_ShouldReturnLowStockProducts() {
            Product sufficientStock = createProduct("Producto Suficiente", "Test",
                    BigDecimal.valueOf(100.0), 10, 5);
            entityManager.persistAndFlush(sufficientStock);

            List<Product> results = productRepository.findProductsWithLowStock();

            assertThat(results)
                    .hasSize(1)
                    .extracting(Product::getName)
                    .containsExactly("Teclado Mecánico");
        }

        @Test
        @DisplayName("Debe encontrar productos sin stock")
        void findProductsOutOfStock_ShouldReturnOutOfStockProducts() {
            List<Product> results = productRepository.findProductsOutOfStock();

            assertThat(results)
                    .hasSize(2)
                    .extracting(Product::getName)
                    .containsExactlyInAnyOrder("Monitor 4K", "Producto Agotado");
        }

        @Test
        @DisplayName("Debe encontrar productos activos (con stock > 0)")
        void findActiveProducts_ShouldReturnProductsWithStock() {
            List<Product> results = productRepository.findActiveProducts();

            assertThat(results).hasSize(3);
            assertThat(results).extracting(Product::getStock)
                    .allMatch(stock -> stock > 0);
        }

        @Test
        @DisplayName("Debe ordenar productos por stock ascendente")
        void findAllByOrderByStockAsc_ShouldReturnProductsOrderedByStock() {
            List<Product> results = productRepository.findAllByOrderByStockAsc();

            assertThat(results).isNotEmpty();
            for (int i = 0; i < results.size() - 1; i++) {
                assertThat(results.get(i).getStock())
                        .isLessThanOrEqualTo(results.get(i + 1).getStock());
            }
        }
    }

    @Nested
    @DisplayName("Búsqueda por rango de precios")
    class PriceRangeTests {

        @Test
        @DisplayName("Debe encontrar productos en rango de precios")
        void findByPriceBetween_ShouldReturnProductsInPriceRange() {
            List<Product> results = productRepository.findByPriceBetween(20.0, 100.0);

            assertThat(results).hasSize(3);
            assertThat(results).extracting(Product::getPrice)
                    .allMatch(price -> price.doubleValue() >= 20.0 && price.doubleValue() <= 100.0);
        }

        @Test
        @DisplayName("Debe retornar lista vacía para rango sin productos")
        void findByPriceBetween_ShouldReturnEmptyForRangeWithoutProducts() {
            List<Product> results = productRepository.findByPriceBetween(2000.0, 3000.0);

            assertThat(results).isEmpty();
        }

        @Test
        @DisplayName("Debe manejar rango de precios con límites iguales")
        void findByPriceBetween_ShouldHandleEqualLimits() {
            List<Product> results = productRepository.findByPriceBetween(25.0, 25.0);

            assertThat(results).hasSize(1);
            assertThat(results.get(0).getPrice()).isEqualTo(BigDecimal.valueOf(25.0));
        }
    }

    @Nested
    @DisplayName("Búsqueda combinada con paginación")
    class CombinedSearchWithPaginationTests {

        @Test
        @DisplayName("Debe buscar por categoría y nombre con paginación")
        void findByCategoryAndName_ShouldReturnFilteredAndPagedResults() {
            Pageable pageable = PageRequest.of(0, 2);
            Page<Product> results = productRepository.findByCategoryAndName("Electronics", "a", pageable);

            assertThat(results.getContent()).hasSize(2);
            assertThat(results.getContent()).extracting(Product::getCategory)
                    .allMatch(category -> category.equalsIgnoreCase("Electronics"));
            assertThat(results.getContent()).extracting(Product::getName)
                    .allMatch(name -> name.toLowerCase().contains("a"));
        }

        @Test
        @DisplayName("Debe buscar solo por categoría cuando nombre es null")
        void findByCategoryAndName_ShouldFilterByCategoryOnly() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<Product> results = productRepository.findByCategoryAndName("Electronics", null, pageable);

            assertThat(results.getContent()).hasSize(4);
            assertThat(results.getContent()).extracting(Product::getCategory)
                    .allMatch(category -> category.equalsIgnoreCase("Electronics"));
        }

        @Test
        @DisplayName("Debe buscar solo por nombre cuando categoría es null")
        void findByCategoryAndName_ShouldFilterByNameOnly() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<Product> results = productRepository.findByCategoryAndName(null, "mouse", pageable);

            assertThat(results.getContent()).hasSize(1);
            assertThat(results.getContent().get(0).getName()).containsIgnoringCase("mouse");
        }

        @Test
        @DisplayName("Debe retornar todos los productos cuando ambos parámetros son null")
        void findByCategoryAndName_ShouldReturnAllWhenBothParamsNull() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<Product> results = productRepository.findByCategoryAndName(null, null, pageable);

            assertThat(results.getContent()).hasSize(5);
        }
    }

    @Nested
    @DisplayName("Verificación de existencia")
    class ExistenceTests {

        @Test
        @DisplayName("Debe verificar existencia de producto por nombre")
        void existsByNameIgnoreCase_ShouldReturnTrueForExistingProduct() {
            assertThat(productRepository.existsByNameIgnoreCase("laptop gaming")).isTrue();
            assertThat(productRepository.existsByNameIgnoreCase("LAPTOP GAMING")).isTrue();
            assertThat(productRepository.existsByNameIgnoreCase("producto inexistente")).isFalse();
        }
    }

    @Nested
    @DisplayName("Consultas de categorías")
    class CategoryQueriesTests {

        @Test
        @DisplayName("Debe obtener todas las categorías únicas ordenadas")
        void findAllCategories_ShouldReturnUniqueOrderedCategories() {
            List<String> categories = productRepository.findAllCategories();

            assertThat(categories).containsExactly("Electronics", "Office");
        }

        @Test
        @DisplayName("Debe contar productos por categoría")
        void countProductsByCategory_ShouldReturnCorrectCounts() {
            List<Object[]> results = productRepository.countProductsByCategory();

            assertThat(results).hasSize(2);

            boolean foundElectronics = false;
            boolean foundOffice = false;

            for (Object[] result : results) {
                String category = (String) result[0];
                Long count = (Long) result[1];

                if ("Electronics".equals(category)) {
                    assertThat(count).isEqualTo(4L);
                    foundElectronics = true;
                } else if ("Office".equals(category)) {
                    assertThat(count).isEqualTo(1L);
                    foundOffice = true;
                }
            }

            assertThat(foundElectronics).isTrue();
            assertThat(foundOffice).isTrue();
        }
    }

    @Nested
    @DisplayName("Casos extremos y manejo de errores")
    class EdgeCasesTests {

        @Test
        @DisplayName("Debe manejar base de datos vacía")
        void shouldHandleEmptyDatabase() {
            productRepository.deleteAll();
            entityManager.flush();

            assertThat(productRepository.findByNameContainingIgnoreCase("cualquier")).isEmpty();
            assertThat(productRepository.findByCategoryIgnoreCase("cualquier")).isEmpty();
            assertThat(productRepository.findProductsWithLowStock()).isEmpty();
            assertThat(productRepository.findProductsOutOfStock()).isEmpty();
            assertThat(productRepository.findAllCategories()).isEmpty();
            assertThat(productRepository.existsByNameIgnoreCase("cualquier")).isFalse();
        }

        @Test
        @DisplayName("Debe manejar strings vacíos en búsqueda")
        void shouldHandleEmptyStringsInSearch() {
            List<Product> results = productRepository.findByNameContainingIgnoreCase("");

            assertThat(results).hasSize(5);
        }

        @Test
        @DisplayName("Debe manejar productos sin minimum stock definido")
        void shouldHandleProductsWithoutMinimumStock() {
            Product noMinStock = createProduct("Sin Mínimo", "Test", BigDecimal.valueOf(100.0), 5, null);
            entityManager.persistAndFlush(noMinStock);

            List<Product> lowStockResults = productRepository.findProductsWithLowStock();

            assertThat(lowStockResults).extracting(Product::getName)
                    .doesNotContain("Sin Mínimo");
        }
    }



}