package fit.iuh.se.servicesImpl;

import fit.iuh.se.dtos.PromotionDTO;
import fit.iuh.se.dtos.PromotionDetailDTO;
import fit.iuh.se.entities.PromotionBase;
import fit.iuh.se.entities.PromotionDetail;
import fit.iuh.se.entities.Product;
import fit.iuh.se.repositories.PromotionBaseRepository;
import fit.iuh.se.repositories.PromotionDetailRepository;
import fit.iuh.se.repositories.ProductRepository;
import fit.iuh.se.services.PromotionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PromotionServiceImpl implements PromotionService {
    @Autowired
    private PromotionBaseRepository promotionRepo;
    @Autowired
    private PromotionDetailRepository promotionDetailRepo;
    @Autowired
    private ProductRepository productRepo;

    @Override
    public List<PromotionDTO> findAll() {
        return promotionRepo.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public PromotionDTO findById(int id) {
        PromotionBase promo = promotionRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy khuyến mãi"));
        return convertToDTO(promo);
    }

    @Override
    public PromotionDTO create(PromotionDTO dto) {
        PromotionBase promo = new PromotionBase();
        promo.setName(dto.getName());
        promo.setCode(dto.getCode());
        promo.setDiscountType(dto.getDiscountType());
        promo.setDiscountValue(dto.getDiscountValue());
        promo.setStartDate(dto.getStartDate());
        promo.setEndDate(dto.getEndDate());
        promo.setIsActive(true);
        promotionRepo.save(promo);
        return convertToDTO(promo);
    }

    @Override
    public PromotionDTO update(int id, PromotionDTO dto) {
        PromotionBase promo = promotionRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy khuyến mãi"));

        promo.setName(dto.getName());
        promo.setCode(dto.getCode());
        promo.setDiscountType(dto.getDiscountType());
        promo.setDiscountValue(dto.getDiscountValue());
        promo.setStartDate(dto.getStartDate());
        promo.setEndDate(dto.getEndDate());
        promo.setIsActive(dto.getIsActive());

        promotionRepo.save(promo);
        return convertToDTO(promo);
    }

    @Override
    public void delete(int id) {
        if (!promotionRepo.existsById(id))
            throw new RuntimeException("Không tìm thấy khuyến mãi để xóa!");
        promotionRepo.deleteById(id);
    }

    @Override
    public PromotionDTO assignProducts(int promotionId, List<Integer> productIds) {
        PromotionBase promo = promotionRepo.findById(promotionId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy khuyến mãi"));

        List<Product> products = productRepo.findAllById(productIds);
        if (products.isEmpty())
            throw new RuntimeException("Danh sách sản phẩm trống!");

        promotionDetailRepo.findByPromotionId(promotionId)
                .forEach(detail -> promotionDetailRepo.deleteById(detail.getId()));

        for (Product p : products) {
            PromotionDetail detail = new PromotionDetail();
            detail.setPromotion(promo);
            detail.setProduct(p);
            detail.setMinOrderValue(0.0);
            promotionDetailRepo.save(detail);
        }

        return convertToDTO(promo);
    }

    private PromotionDTO convertToDTO(PromotionBase promo) {
        List<PromotionDetailDTO> details = promotionDetailRepo.findByPromotionId(promo.getId())
                .stream()
                .map(d -> PromotionDetailDTO.builder()
                        .id(d.getId())
                        .productId(d.getProduct().getId())
                        .productName(d.getProduct().getName())
                        .promotionId(promo.getId())
                        .promotionName(promo.getName())
                        .minOrderValue(d.getMinOrderValue())
                        .build())
                .collect(Collectors.toList());

        return PromotionDTO.builder()
                .id(promo.getId())
                .name(promo.getName())
                .code(promo.getCode())
                .discountType(promo.getDiscountType())
                .discountValue(promo.getDiscountValue())
                .startDate(promo.getStartDate())
                .endDate(promo.getEndDate())
                .isActive(promo.getIsActive())
                .details(details)
                .build();
    }
}
