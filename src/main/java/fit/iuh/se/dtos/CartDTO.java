package fit.iuh.se.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CartDTO {
    private int id;
    private int userId;
    private List<CartItemDTO> items;
    private double total;
}