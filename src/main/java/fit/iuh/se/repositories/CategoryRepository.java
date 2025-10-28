package fit.iuh.se.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import fit.iuh.se.entities.Category;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Integer> {
    Optional<Category> findByName(String name);

    Page<Category> findByNameContainingIgnoreCase(String name, Pageable pageable);

    @Query("SELECT c FROM Category c WHERE SIZE(c.products) = 0")
    List<Category> findCategoriesWithoutProducts();

    boolean existsByNameAndIdNot(String name, int id);
}
