package fit.iuh.se.controllers;

import fit.iuh.se.dtos.ProductDTO;
import fit.iuh.se.services.ProductService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class ProductController {

    @Autowired
    private ProductService productService;

    @GetMapping("/products")
    public ResponseEntity<?> getAllProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ProductDTO> products = productService.findAllActive(pageable);

        if (products.isEmpty()) {
            return ResponseEntity.ok(Map.of("message", "Không có sản phẩm nào!"));
        }
        return ResponseEntity.ok(products);
    }

    @GetMapping("/products/{id}")
    public ResponseEntity<?> getProductById(@PathVariable int id) {
        try {
            ProductDTO product = productService.findById(id);
            return ResponseEntity.ok(product);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/products/category/{id}")
    public ResponseEntity<?> getProductsByCategory(
            @PathVariable int id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ProductDTO> products = productService.findByCategory(id, pageable);
        if (products.isEmpty()) {
            return ResponseEntity.ok(Map.of("message", "Không có sản phẩm nào trong danh mục này!"));
        }
        return ResponseEntity.ok(products);
    }

    @GetMapping("/products/search")
    public ResponseEntity<?> searchProductsByName(
            @RequestParam String name,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ProductDTO> products = productService.searchByName(name, pageable);

        if (products.isEmpty()) {
            return ResponseEntity.ok(Map.of("message", "Không tìm thấy sản phẩm nào phù hợp!"));
        }

        return ResponseEntity.ok(products);
    }

    @GetMapping("/products/price-range")
    public ResponseEntity<?> findByPriceRange(
            @RequestParam BigDecimal min,
            @RequestParam BigDecimal max,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ProductDTO> products = productService.findByPriceRange(min, max, pageable);

        if (products.isEmpty()) {
            return ResponseEntity.ok(Map.of("message", "Không có sản phẩm nào trong khoảng giá này!"));
        }
        return ResponseEntity.ok(products);
    }

    @GetMapping("/admin/products")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getAllProductsForAdmin(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ProductDTO> pageData = productService.findAll(pageable);
        List<ProductDTO> products = pageData.getContent();
        return ResponseEntity.ok(products);
    }

    @GetMapping("/admin/products/active")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getAllActiveProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ProductDTO> products = productService.findAllActive(pageable);

        if (products.isEmpty()) {
            return ResponseEntity.ok(Map.of("message", "Hiện tại không có sản phẩm nào hoạt động!"));
        }
        return ResponseEntity.ok(products);
    }

    @PostMapping("/admin/products")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createProduct(@Valid @RequestBody ProductDTO dto, BindingResult result) {
        if (result.hasErrors()) {
            Map<String, String> errors = new HashMap<>();
            for (FieldError err : result.getFieldErrors()) {
                errors.put(err.getField(), err.getDefaultMessage());
            }
            return ResponseEntity.badRequest().body(Map.of("status", 400, "errors", errors));
        }

        ProductDTO saved = productService.save(dto);
        return ResponseEntity.ok(Map.of(
                "message", "Tạo sản phẩm thành công!",
                "product", saved
        ));
    }

    @PutMapping("/admin/products/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateProduct(@PathVariable int id, @Valid @RequestBody ProductDTO dto, BindingResult result) {
        if (result.hasErrors()) {
            Map<String, String> errors = new HashMap<>();
            for (FieldError err : result.getFieldErrors()) {
                errors.put(err.getField(), err.getDefaultMessage());
            }
            return ResponseEntity.badRequest().body(Map.of("status", 400, "errors", errors));
        }
        try {
            ProductDTO updated = productService.update(id, dto);
            return ResponseEntity.ok(Map.of("message", "Cập nhật sản phẩm thành công!", "product", updated));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }


    @DeleteMapping("/admin/products/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteProduct(@PathVariable int id) {
        try {
            productService.delete(id);
            return ResponseEntity.ok(Map.of("message", "Xóa sản phẩm thành công!"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/admin/products/price-range")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> findByPriceRangeForAdmin(
            @RequestParam BigDecimal min,
            @RequestParam BigDecimal max,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ProductDTO> products = productService.findByPriceRange(min, max, pageable);

        if (products.isEmpty()) {
            return ResponseEntity.ok(Map.of("message", "Không có sản phẩm nào trong khoảng giá này!"));
        }
        return ResponseEntity.ok(products);
    }

    @PutMapping("/admin/products/{id}/stock")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateStock(
            @PathVariable int id,
            @RequestParam Integer quantity
    ) {
        try {
            productService.updateStock(id, quantity);
            return ResponseEntity.ok(Map.of("message", "Cập nhật tồn kho thành công!"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}