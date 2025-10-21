package fit.iuh.se.dtos;

import java.math.BigDecimal;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public final class ProductDTO {
    private int id;

    @NotBlank(message = "Tên sản phẩm không được để trống")
    @Size(max = 200, message = "Tên sản phẩm không vượt quá 200 ký tự")
    private String name;

    private String description;

    @NotNull(message = "Giá sản phẩm là bắt buộc")
    @DecimalMin(value = "0.01", message = "Giá phải lớn hơn 0")
    private BigDecimal price;

    @NotNull(message = "Số lượng tồn kho là bắt buộc")
    @Min(value = 0, message = "Số lượng phải lớn hơn hoặc bằng 0")
    private Integer stock;

    @Pattern(regexp = "^$|^(https?|ftp)://.*$", message = "Đường dẫn hình ảnh phải là URL hợp lệ hoặc để trống")
    private String imageUrl;

    private Boolean isActive = true;

    @NotNull(message = "Danh mục sản phẩm là bắt buộc")
    private Integer categoryId;
}
