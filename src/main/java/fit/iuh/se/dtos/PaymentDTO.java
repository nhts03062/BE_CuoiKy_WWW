package fit.iuh.se.dtos;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class PaymentDTO {
    private int id;
    private int orderId;
    private String transactionRef;
    private Double amount;
    private String method;
    private String status;
    private LocalDateTime paymentDate;
    private int userId;
}
