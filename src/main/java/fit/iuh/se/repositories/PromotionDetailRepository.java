package fit.iuh.se.repositories;

import fit.iuh.se.entities.PromotionDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PromotionDetailRepository extends JpaRepository<PromotionDetail, Integer> {
    List<PromotionDetail> findByPromotionId(int promotionId);
    List<PromotionDetail> findByProductId(int productId);
    @Query("""
        SELECT pd FROM PromotionDetail pd
        JOIN FETCH pd.product p
        JOIN FETCH pd.promotion promo
        WHERE promo.isActive = true
        AND CURRENT_DATE BETWEEN promo.startDate AND promo.endDate
        """
    )
    List<PromotionDetail> findActivePromotions();

}
