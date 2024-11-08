package org.example.ecommerce.repositories;

import org.example.ecommerce.dtos.ProductResponseDTO;
import org.example.ecommerce.models.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Map;


public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {
    @Query("SELECT p FROM Product p WHERE p.specsId = ?1")
    Product findBySpecsId(String specsId);

    @Query("SELECT p FROM Product p WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) AND p.deleted = false")
    Page<Product> searchProductsByName(@Param("keyword") String keyword, Pageable pageable);

    Page<Product> findByNameContainingIgnoreCaseAndPriceBetween(
            String name, int minPrice, int maxPrice, Pageable pageable);
    @Query("SELECT p FROM Product p WHERE "
            + "p.deleted = false AND "
            + "(:name IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%'))) AND "
            + "(:minPrice IS NULL OR p.price >= :minPrice) AND "
            + "(:maxPrice IS NULL OR p.price <= :maxPrice) AND "
            + "(:category IS NULL OR p.subCategory.category.name = :category) AND "
            + "(:subCategory IS NULL OR p.subCategory.name = :subCategory)")
    Page<Product> searchProducts(
            @Param("name") String name,
            @Param("minPrice") Integer minPrice,
            @Param("maxPrice") Integer maxPrice,
            @Param("category") String category,
            @Param("subCategory") String subCategory,
            Pageable pageable);


    @Query("SELECT p FROM Product p WHERE p.subCategory.id = :subCategoryId AND p.deleted = false")
    Page<Product> findBySubCategory(@Param("subCategoryId") Long subCategoryId, Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.subCategory.id = :subCategoryId AND p.specsId IN :specsIds AND p.deleted = false")
    Page<Product> findBySubCategoryIdAndSpecsIds(
            @Param("subCategoryId") Long subCategoryId,
            @Param("specsIds") List<String> specsIds,
            Pageable pageable);

    Page<Product> findBySubCategoryIdAndNameLikeIgnoreCaseAndDeletedFalse(
            Long subCategoryId,
            String name,
            Pageable pageable);



    Page<Product> findAllByDeleted(boolean deleted, Pageable pageable);


    // fetching latest products
    @Query("SELECT p FROM Product p WHERE p.deleted = false ORDER BY p.createdAt DESC limit 10")
    List<Product> findTop10ByOrderByCreatedAtDesc();

    // fetching products on sale
    @Query("SELECT p FROM Product p WHERE p.deleted = false AND p.salePercentage > 0 ORDER BY p.createdAt DESC")
    Page<Product> findProductsOnSale(Pageable pageable);

    // fetching products on flash sale
    @Query("SELECT p FROM Product p WHERE p.salePercentage > 50 AND p.deleted = false")
    Page<Product> findFlashSaleProducts(Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.subCategory.id = :subCategoryId AND p.deleted = false")
    Page<Product> findBySubCategoryId(@Param("subCategoryId") Long subCategoryId, Pageable pageable);


}


