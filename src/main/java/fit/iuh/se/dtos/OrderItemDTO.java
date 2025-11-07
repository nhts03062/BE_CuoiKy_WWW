package fit.iuh.se.dtos;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItemDTO {
    private int id;
    private int productId;
    private String productName;
    private int quantity;
    private double price;
    private double subTotal;
}