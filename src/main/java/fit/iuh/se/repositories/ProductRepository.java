package fit.iuh.se.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import fit.iuh.se.entities.Product;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Integer> {
    @Query("SELECT p FROM Product p WHERE p.isActive = true AND p.stock > 0 AND p.price BETWEEN :min AND :max")
    Page<Product> findByPriceRange(BigDecimal min, BigDecimal max, Pageable pageable);

    Page<Product> findByIsActiveTrue(Pageable pageable);

    Page<Product> findByCategoryIdAndIsActiveTrue(int categoryId, Pageable pageable);

    Page<Product> findByNameContainingIgnoreCaseAndIsActiveTrue(String name, Pageable pageable);

    List<Product> findByCategoryId(int categoryId);

    Optional<Product> findByNameIgnoreCaseAndCategoryId(String name, int categoryId);

    int countById(int id);
}