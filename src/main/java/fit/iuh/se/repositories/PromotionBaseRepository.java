package fit.iuh.se.repositories;

import fit.iuh.se.entities.PromotionBase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PromotionBaseRepository extends JpaRepository<PromotionBase, Integer> {
    Optional<PromotionBase>findByCode(String code);
}
