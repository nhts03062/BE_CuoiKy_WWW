package fit.iuh.se.services;

import fit.iuh.se.dtos.OrderDTO;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;

public interface VnPayService {
    public String createPaymentUrl(OrderDTO order, HttpServletRequest request);
    public boolean handleCallback(Map<String, String> params);
    public String hmacSHA512(String key, String data);
    public String getClientIpAddress(HttpServletRequest request);
}
