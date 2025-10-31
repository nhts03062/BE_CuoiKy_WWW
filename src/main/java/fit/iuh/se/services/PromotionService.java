package fit.iuh.se.services;

import fit.iuh.se.dtos.PromotionDTO;
import java.util.List;

public interface PromotionService {
    List<PromotionDTO> findAll();
    PromotionDTO findById(int id);
    PromotionDTO create(PromotionDTO dto);
    PromotionDTO update(int id, PromotionDTO dto);
    void delete(int id);
    PromotionDTO assignProducts(int promotionId, List<Integer> productIds);
}
