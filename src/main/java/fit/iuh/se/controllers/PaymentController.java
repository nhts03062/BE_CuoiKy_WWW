package fit.iuh.se.controllers;

import fit.iuh.se.config.VnPayConfig;
import fit.iuh.se.dtos.OrderDTO;
import fit.iuh.se.services.OrderService;
import fit.iuh.se.services.VnPayService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Controller
@RequestMapping("/payment")
@RequiredArgsConstructor
public class PaymentController {
    @Autowired
    private VnPayService vnPayService;
    @Autowired
    private OrderService orderService;
    @Autowired
    private VnPayConfig vnPayConfig;

    @PostMapping("/init/{orderId}")
    public ResponseEntity<?> initPayment(@PathVariable int orderId, HttpServletRequest request) {
        OrderDTO order = orderService.findEntityById(orderId);
        String paymentUrl = vnPayService.createPaymentUrl(order, request);
        return ResponseEntity.ok(Map.of("payment_url", paymentUrl));
    }

    @GetMapping("/callback")
    public String handleCallback(@RequestParam Map<String, String> params) {
        try {
            String orderCode = params.get("vnp_TxnRef");
            boolean success = vnPayService.handleCallback(params);
            
            String frontendUrl = vnPayConfig.getFrontendUrl();
            
            if (success) {
                // Redirect to order success page with order code
                String redirectUrl = frontendUrl + "/order-success";
                if (orderCode != null && !orderCode.isEmpty()) {
                    redirectUrl += "?orderCode=" + URLEncoder.encode(orderCode, StandardCharsets.UTF_8);
                }
                return "redirect:" + redirectUrl;
            } else {
                // Redirect to order failed page with error message
                String errorMessage = params.getOrDefault("vnp_ResponseCode", "Unknown error");
                String redirectUrl = frontendUrl + "/order-failed?message=" 
                    + URLEncoder.encode("Thanh toán thất bại hoặc bị hủy! Mã lỗi: " + errorMessage, StandardCharsets.UTF_8)
                    + "&code=" + URLEncoder.encode(errorMessage, StandardCharsets.UTF_8);
                if (orderCode != null && !orderCode.isEmpty()) {
                    redirectUrl += "&orderCode=" + URLEncoder.encode(orderCode, StandardCharsets.UTF_8);
                }
                return "redirect:" + redirectUrl;
            }
        } catch (Exception e) {
            // Redirect to order failed page with exception message
            String frontendUrl = vnPayConfig.getFrontendUrl();
            String redirectUrl = frontendUrl + "/order-failed?message=" 
                + URLEncoder.encode("Lỗi callback: " + e.getMessage(), StandardCharsets.UTF_8)
                + "&code=CALLBACK_ERROR";
            return "redirect:" + redirectUrl;
        }
    }
}