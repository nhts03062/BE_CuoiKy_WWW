package fit.iuh.se.controllers;

import fit.iuh.se.dtos.PromotionDTO;
import fit.iuh.se.services.PromotionService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin/promotions")
@PreAuthorize("hasRole('ADMIN')")
public class PromotionController {

    @Autowired
    private PromotionService promotionService;

    @GetMapping
    public ResponseEntity<?> getAll() {
        return ResponseEntity.ok(promotionService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable int id) {
        try {
            PromotionDTO dto = promotionService.findById(id);
            return ResponseEntity.ok(dto);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping
    public ResponseEntity<?> create(
            @Valid @RequestBody PromotionDTO dto,
            BindingResult result
    ) {
        if (result.hasErrors()) {
            Map<String, String> errors = new HashMap<>();
            for (FieldError err : result.getFieldErrors()) {
                errors.put(err.getField(), err.getDefaultMessage());
            }
            return ResponseEntity.badRequest().body(Map.of(
                    "status", 400,
                    "errors", errors
            ));
        }

        try {
            PromotionDTO created = promotionService.create(dto);
            return ResponseEntity.ok(Map.of(
                    "message", "Tạo khuyến mãi thành công!",
                    "promotion", created
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(
            @PathVariable int id,
            @Valid @RequestBody PromotionDTO dto,
            BindingResult result
    ) {
        if (result.hasErrors()) {
            Map<String, String> errors = new HashMap<>();
            for (FieldError err : result.getFieldErrors()) {
                errors.put(err.getField(), err.getDefaultMessage());
            }
            return ResponseEntity.badRequest().body(Map.of(
                    "status", 400,
                    "errors", errors
            ));
        }

        try {
            PromotionDTO updated = promotionService.update(id, dto);
            return ResponseEntity.ok(Map.of(
                    "message", "Cập nhật khuyến mãi thành công!",
                    "promotion", updated
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable int id) {
        try {
            promotionService.delete(id);
            return ResponseEntity.ok(Map.of("message", "Đã xóa khuyến mãi thành công!"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/{id}/assign-products")
    public ResponseEntity<?> assignProducts(
            @PathVariable int id,
            @RequestBody List<Integer> productIds
    ) {
        try {
            return ResponseEntity.ok(Map.of(
                    "message", "Đã gán sản phẩm vào khuyến mãi thành công!",
                    "result", promotionService.assignProducts(id, productIds)
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
