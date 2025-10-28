package fit.iuh.se.controllers;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import fit.iuh.se.dtos.CartItemDTO;
import fit.iuh.se.dtos.OrderDTO;
import fit.iuh.se.services.CartService;
import fit.iuh.se.services.VnPayService;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/cart")
public class CartController {

    @Autowired
    private CartService cartService;
    
    @Autowired
    private VnPayService vnPayService;

    @PostMapping("/add")
    public ResponseEntity<?> addToCart(@RequestBody Map<String, Object> body) {
        try {
            int productId = (int) body.get("productId");
            Integer quantity = body.get("quantity") != null
                    ? (int) body.get("quantity")
                    : 1;

            cartService.addToCart(productId, quantity);
            return ResponseEntity.ok(Map.of("message", "Đã thêm sản phẩm vào giỏ hàng"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "status", 400,
                    "message", "Lỗi: " + e.getMessage()));
        }
    }

    @PatchMapping("/update")
    public ResponseEntity<?> updateCartItem(@RequestBody Map<String, Object> body) {
        try {
            int productId = (int) body.get("productId");
            Integer quantity = (int) body.get("quantity");

            cartService.updateCartItem(productId, quantity);
            return ResponseEntity.ok(Map.of("message", "Đã cập nhật giỏ hàng"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "status", 400,
                    "message", "Lỗi: " + e.getMessage()));
        }
    }

    @DeleteMapping("/remove")
    public ResponseEntity<?> removeFromCart(@RequestBody Map<String, Object> body) {
        try {
            int productId = (int) body.get("productId");
            cartService.removeFromCart(productId);
            return ResponseEntity.ok(Map.of("message", "Đã xóa sản phẩm khỏi giỏ hàng"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "status", 400,
                    "message", "Lỗi: " + e.getMessage()));
        }
    }

    @DeleteMapping("/clear")
    public ResponseEntity<?> clearCart() {
        cartService.clearCart();
        return ResponseEntity.ok(Map.of("message", "Đã xóa toàn bộ giỏ hàng"));
    }

    @GetMapping("/items")
    public ResponseEntity<List<CartItemDTO>> getCartItems() {
        return ResponseEntity.ok(cartService.getCartItems());
    }

    @GetMapping("/item")
    public ResponseEntity<List<CartItemDTO>> getCartItem() {
        return ResponseEntity.ok(cartService.getCartItems());
    }

    @GetMapping("/total")
    public ResponseEntity<Map<String, Double>> getCartTotal() {
        return ResponseEntity.ok(Map.of("total", cartService.getCartTotal()));
    }

    @GetMapping("/count")
    public ResponseEntity<Map<String, Integer>> getCartItemCount() {
        return ResponseEntity.ok(Map.of("count", cartService.getCartItemCount()));
    }

    @GetMapping("/empty")
    public ResponseEntity<Map<String, Boolean>> isCartEmpty() {
        return ResponseEntity.ok(Map.of("empty", cartService.isCartEmpty()));
    }

    @PostMapping("/checkout")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<?> checkout(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        try {
            if (body.get("userId") == null) {
                return ResponseEntity.badRequest().body(Map.of(
                        "status", 400,
                        "message", "userId không được để trống"));
            }
            if (body.get("paymentMethod") == null) {
                return ResponseEntity.badRequest().body(Map.of(
                        "status", 400,
                        "message", "paymentMethod không được để trống"));
            }
            if (body.get("shippingAddress") == null) {
                return ResponseEntity.badRequest().body(Map.of(
                        "status", 400,
                        "message", "shippingAddress không được để trống"));
            }

            int userId = (int) body.get("userId");
            String paymentMethod = body.get("paymentMethod").toString();
            String shippingAddress = body.get("shippingAddress").toString();
            String note = body.get("note") != null ? body.get("note").toString() : null;

            OrderDTO createdOrder = cartService.checkout(userId, paymentMethod, shippingAddress, note);

            if (paymentMethod.equalsIgnoreCase("VNPAY")) {
                String paymentUrl = vnPayService.createPaymentUrl(createdOrder, request);
                return ResponseEntity.ok(Map.of(
                        "status", 200,
                        "payment_url", paymentUrl));
            }

            return ResponseEntity.ok(Map.of(
                    "status", 200,
                    "order", createdOrder));

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "status", 400,
                    "message", "Lỗi khi checkout: " + e.getMessage()));
        }
    }

}