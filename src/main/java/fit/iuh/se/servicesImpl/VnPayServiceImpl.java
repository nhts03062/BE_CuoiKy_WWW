package fit.iuh.se.servicesImpl;

import fit.iuh.se.config.VnPayConfig;
import fit.iuh.se.dtos.OrderDTO;
import fit.iuh.se.dtos.PaymentDTO;
import fit.iuh.se.entities.Order;
import fit.iuh.se.entities.Payment;
import fit.iuh.se.enums.OrderStatus;
import fit.iuh.se.enums.PaymentStatus;
import fit.iuh.se.repositories.OrderRepository;
import fit.iuh.se.repositories.PaymentRepository;
import fit.iuh.se.services.VnPayService;
import org.apache.commons.codec.binary.Hex;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import jakarta.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class VnPayServiceImpl implements VnPayService {

    @Autowired
    private VnPayConfig config;
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private PaymentRepository paymentRepository;

    public String createPaymentUrl(OrderDTO order, HttpServletRequest request) {
        Map<String, String> params = new LinkedHashMap<>();
        params.put("vnp_Version", config.getVnpVersion());
        params.put("vnp_Command", config.getCommand());
        params.put("vnp_TmnCode", config.getTmnCode());
        params.put("vnp_Amount", BigDecimal.valueOf(order.getFinalAmount())
                .multiply(BigDecimal.valueOf(100)).toBigInteger().toString());
        params.put("vnp_CurrCode", "VND");
        params.put("vnp_TxnRef", order.getOrderCode());
        params.put("vnp_OrderInfo", "Thanh toan don hang: " + order.getOrderCode());
        params.put("vnp_OrderType", "billpayment");
        params.put("vnp_Locale", "vn");
        params.put("vnp_ReturnUrl", config.getReturnUrl());
        // Get real client IP address instead of hardcoding
        params.put("vnp_IpAddr", getClientIpAddress(request));
        params.put("vnp_CreateDate", LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")));

        String hashData = params.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(e -> e.getKey() + "=" + URLEncoder.encode(e.getValue(), StandardCharsets.US_ASCII))
                .collect(Collectors.joining("&"));

        String query = params.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(e -> e.getKey() + "=" + URLEncoder.encode(e.getValue(), StandardCharsets.US_ASCII))
                .collect(Collectors.joining("&"));

        String secureHash = hmacSHA512(config.getHashSecret(), hashData);

        return config.getPaymentUrl() + "?" + query + "&vnp_SecureHash=" + secureHash;
    }

    @Override
    public boolean handleCallback(Map<String, String> params) {
        String orderCode = params.get("vnp_TxnRef");
        String responseCode = params.get("vnp_ResponseCode");

        Optional<Order> orderOpt = orderRepository.findByOrderCode(orderCode);
        if (orderOpt.isEmpty()) {
            return false;
        }

        Order order = orderOpt.get();

        Payment payment = order.getPayments() != null && !order.getPayments().isEmpty()
                ? order.getPayments().get(0)
                : null;

        if ("00".equals(responseCode)) {
            order.setPaymentStatus(PaymentStatus.SUCCESS.name());
            order.setStatus(OrderStatus.CONFIRMED.name());

            if (payment != null) {
                payment.setStatus(PaymentStatus.SUCCESS.name());
                payment.setPaymenyDate(LocalDateTime.now());
                paymentRepository.save(payment);
            }

            orderRepository.save(order);
            return true;
        } else {
            order.setPaymentStatus(PaymentStatus.FAILED.name());
            order.setStatus(OrderStatus.CANCELLED.name());

            if (payment != null) {
                payment.setStatus(PaymentStatus.FAILED.name());
                paymentRepository.save(payment);
            }

            orderRepository.save(order);
            return false;
        }
    }

    @Override
    public String hmacSHA512(String key, String data) {
        try {
            Mac hmac = Mac.getInstance("HmacSHA512");
            SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA512");
            hmac.init(secretKey);
            return Hex.encodeHexString(hmac.doFinal(data.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new RuntimeException("Error while hashing", e);
        }
    }

    @Override
    public String getClientIpAddress(HttpServletRequest request) {
        String ipAddress = request.getHeader("X-Forwarded-For");
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("Proxy-Client-IP");
        }
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getRemoteAddr();
        }
        
        // If multiple IPs, take the first one
        if (ipAddress != null && ipAddress.contains(",")) {
            ipAddress = ipAddress.split(",")[0].trim();
        }
        
        // Fallback to localhost if still null
        if (ipAddress == null || ipAddress.isEmpty()) {
            ipAddress = "127.0.0.1";
        }
        
        return ipAddress;
    }

    public Order convertToEntity(OrderDTO dto) {
        Order order = new Order();
        order.setId(dto.getId());
        order.setOrderCode(dto.getOrderCode());
        order.setDiscountAmount(dto.getDiscountAmount() != null ? dto.getDiscountAmount() : 0);
        order.setFinalAmount(dto.getFinalAmount() != null ? dto.getFinalAmount() : 0);
        order.setPaymentMethod(dto.getPaymentMethod());
        order.setPaymentStatus(dto.getPaymentStatus());
        order.setStatus(dto.getStatus());
        order.setShippingAddress(dto.getShippingAddress());
        order.setNote(dto.getNote());
        return order;
    }

    public PaymentDTO convertToDTO(Payment payment) {
        PaymentDTO dto = new PaymentDTO();
        dto.setId(payment.getId());
        dto.setOrderId(payment.getOrder().getId());
        dto.setTransactionRef(payment.getTransactionRef());
        dto.setAmount(payment.getAmount());
        dto.setMethod(payment.getMethod());
        dto.setStatus(payment.getStatus());
        dto.setPaymentDate(payment.getPaymenyDate());
        dto.setUserId(payment.getUserAccount().getId());
        return dto;
    }
}