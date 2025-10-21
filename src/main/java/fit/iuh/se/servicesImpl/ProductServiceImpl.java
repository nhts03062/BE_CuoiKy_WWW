package fit.iuh.se.servicesImpl;

import fit.iuh.se.dtos.ProductDTO;
import fit.iuh.se.entities.Category;
import fit.iuh.se.entities.Product;
import fit.iuh.se.repositories.CategoryRepository;
import fit.iuh.se.repositories.OrderItemRepository;
import fit.iuh.se.repositories.ProductRepository;
import fit.iuh.se.services.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@Transactional
public class ProductServiceImpl implements ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Override
    public Page<ProductDTO> findAll(Pageable pageable) {
        return productRepository.findAll(pageable).map(this::toDTO);
    }

    @Override
    public Page<ProductDTO> findAllActive(Pageable pageable) {
        Page<ProductDTO> page = productRepository.findByIsActiveTrue(pageable).map(this::toDTO);

        List<ProductDTO> filtered = page.getContent()
                .stream()
                .filter(dto -> dto.getStock() > 0)
                .toList();

        return new PageImpl<>(
                filtered,
                pageable,
                filtered.size());
    }

    @Override
    public ProductDTO findById(int id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm ID: " + id));
        return toDTO(product);
    }

    @Override
    public ProductDTO save(ProductDTO dto) {
        Category category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy danh mục ID: " + dto.getCategoryId()));

        productRepository.findByNameIgnoreCaseAndCategoryId(dto.getName(), dto.getCategoryId())
                .ifPresent(p -> {
                    throw new RuntimeException("Sản phẩm '" + dto.getName() + "' đã tồn tại trong danh mục này!");
                });

        if (dto.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Giá sản phẩm phải lớn hơn 0!");
        }
        if (dto.getStock() < 0) {
            throw new RuntimeException("Số lượng tồn kho không thể âm!");
        }

        Product product = toEntity(dto);
        product.setCategory(category);
        product.setIsActive(true);
        Product saved = productRepository.save(product);

        return toDTO(saved);
    }

    @Override
    public ProductDTO update(int id, ProductDTO dto) {
        Product existing = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm ID: " + id));

        Category category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy danh mục ID: " + dto.getCategoryId()));

        if (dto.getPrice().compareTo(BigDecimal.ZERO) <= 0)
            throw new RuntimeException("Giá sản phẩm phải lớn hơn 0!");
        if (dto.getStock() < 0)
            throw new RuntimeException("Số lượng tồn kho không thể âm!");

        existing.setName(dto.getName());
        existing.setDescription(dto.getDescription());
        existing.setPrice(dto.getPrice());
        existing.setStock(dto.getStock());
        existing.setImageUrl(dto.getImageUrl());
        existing.setIsActive(dto.getIsActive());
        existing.setCategory(category);

        return toDTO(productRepository.save(existing));
    }

    @Override
    public boolean canDelete(int id) {
        return orderItemRepository.countByProductId(id) == 0;
    }

    @Override
    public void updateStock(int productId, Integer quantity) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm ID: " + productId));
        int newStock = product.getStock() - quantity;
        if (newStock < 0) {
            throw new RuntimeException("Không đủ hàng trong kho!");
        }
        product.setStock(newStock);
        if (newStock == 0) {
            product.setIsActive(false);
        }
        productRepository.save(product);
    }

    @Override
    public void delete(int id) {
        if (!canDelete(id)) {
            throw new RuntimeException("Không thể xóa sản phẩm vì đã có trong đơn hàng!");
        }
        productRepository.deleteById(id);
    }

    @Override
    public Page<ProductDTO> findByCategory(int categoryId, Pageable pageable) {
        return productRepository.findByCategoryIdAndIsActiveTrue(categoryId, pageable).map(this::toDTO);
    }

    @Override
    public Page<ProductDTO> searchByName(String name, Pageable pageable) {
        return productRepository.findByNameContainingIgnoreCaseAndIsActiveTrue(name, pageable).map(this::toDTO);
    }

    @Override
    public Page<ProductDTO> findByPriceRange(BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable) {
        return productRepository.findByPriceRange(minPrice, maxPrice, pageable).map(this::toDTO);
    }

    private ProductDTO toDTO(Product p) {
        ProductDTO dto = new ProductDTO();
        dto.setId(p.getId());
        dto.setName(p.getName());
        dto.setDescription(p.getDescription());
        dto.setPrice(p.getPrice());
        dto.setStock(p.getStock());
        dto.setImageUrl(p.getImageUrl());
        dto.setIsActive(p.getIsActive());
        dto.setCategoryId(p.getCategory().getId());
        return dto;
    }

    private Product toEntity(ProductDTO dto) {
        Product p = new Product();
        p.setName(dto.getName());
        p.setDescription(dto.getDescription());
        p.setPrice(dto.getPrice());
        p.setStock(dto.getStock());
        p.setImageUrl(dto.getImageUrl());
        p.setIsActive(dto.getIsActive());
        return p;
    }
}