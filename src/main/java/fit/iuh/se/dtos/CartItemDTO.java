package fit.iuh.se.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CartItemDTO {
    private int id;
    private int productId;
    private String productName;
    private double price;
    private int quantity;
    private double subtotal;
}