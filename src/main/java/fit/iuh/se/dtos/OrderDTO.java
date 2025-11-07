package fit.iuh.se.dtos;

import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderDTO {
    private int id;
    private int userId;
    private String orderCode;
    private Double discountAmount;
    private Double finalAmount;
    private String paymentMethod;
    private String paymentStatus;
    private String status;
    private String shippingAddress;
    private String note;
    private LocalDateTime createdAt;
    private List<OrderItemDTO> items;
}