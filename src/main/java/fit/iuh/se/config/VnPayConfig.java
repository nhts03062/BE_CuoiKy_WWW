package fit.iuh.se.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

@Configuration
@Data
public class VnPayConfig {

    @Value("${vnpay.tmn-code}")
    private String tmnCode;

    @Value("${vnpay.hash-secret}")
    private String hashSecret;

    @Value("${vnpay.payment-url}")
    private String paymentUrl;

    @Value("${vnpay.return-url}")
    private String returnUrl;

    @Value("${vnpay.apiVersion}")
    private String vnpVersion;

    @Value("${vnpay.command}")
    private String command;

    @Value("${frontend.url:http://localhost:3000}")
    private String frontendUrl;
}
