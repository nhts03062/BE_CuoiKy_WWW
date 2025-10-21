package fit.iuh.se.services;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import fit.iuh.se.dtos.ProductDTO;
import java.math.BigDecimal;

public interface ProductService {
    ProductDTO save(ProductDTO productDto);
    ProductDTO update(int id, ProductDTO productDto);
    void delete(int id);

    Page<ProductDTO> findAll(Pageable pageable);
    Page<ProductDTO> findAllActive(Pageable pageable);
    ProductDTO findById(int id);

    Page<ProductDTO> findByCategory(int categoryId, Pageable pageable);
    Page<ProductDTO> searchByName(String name, Pageable pageable);
    Page<ProductDTO> findByPriceRange(BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable);

    boolean canDelete(int id);

    void updateStock(int productId, Integer quantity);
}