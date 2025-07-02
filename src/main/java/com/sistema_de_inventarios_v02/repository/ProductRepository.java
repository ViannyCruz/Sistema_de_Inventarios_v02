package com.sistema_de_inventarios_v02.repository;

import com.sistema_de_inventarios_v02.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    @Query("SELECT p FROM Product p WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<Product> findByNameContainingIgnoreCase(@Param("name") String name);

    List<Product> findByCategoryIgnoreCase(String category);


    /*
    @Query("SELECT p FROM Product p WHERE p.stock <= p.minimumStock AND p.minimumStock IS NOT NULL")
    List<Product> findProductsWithLowStock();*/

    @Query("SELECT p FROM Product p WHERE p.stock > 0 AND p.stock <= p.minimumStock AND p.minimumStock IS NOT NULL")
    List<Product> findProductsWithLowStock();

    @Query("SELECT p FROM Product p WHERE p.stock = 0 OR p.stock IS NULL")
    List<Product> findProductsOutOfStock();

    @Query("SELECT p FROM Product p WHERE p.price BETWEEN :minPrice AND :maxPrice")
    List<Product> findByPriceBetween(@Param("minPrice") Double minPrice, @Param("maxPrice") Double maxPrice);

    @Query("SELECT p FROM Product p WHERE " +
            "(:category IS NULL OR LOWER(p.category) = LOWER(:category)) AND " +
            "(:name IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%')))")
    Page<Product> findByCategoryAndName(@Param("category") String category,
                                        @Param("name") String name,
                                        Pageable pageable);

    boolean existsByNameIgnoreCase(String name);

    @Query("SELECT DISTINCT p.category FROM Product p ORDER BY p.category")
    List<String> findAllCategories();

    @Query("SELECT p.category, COUNT(p) FROM Product p GROUP BY p.category")
    List<Object[]> countProductsByCategory();

    List<Product> findAllByOrderByStockAsc();

    @Query("SELECT p FROM Product p WHERE p.stock > 0")
    List<Product> findActiveProducts();
}