package fit.iuh.se.dtos;

import jakarta.validation.constraints.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PromotionDetailDTO {

    private int id;

    @NotNull(message = "Sản phẩm áp dụng là bắt buộc")
    @Positive(message = "ID sản phẩm phải là số dương")
    private Integer productId;

    private String productName;

    @NotNull(message = "Khuyến mãi là bắt buộc")
    @Positive(message = "ID khuyến mãi phải là số dương")
    private Integer promotionId;

    private String promotionName;

    @DecimalMin(value = "0.0", inclusive = true, message = "Giá trị đơn hàng tối thiểu phải >= 0")
    private Double minOrderValue;
}
