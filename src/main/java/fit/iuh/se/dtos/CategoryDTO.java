package fit.iuh.se.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CategoryDTO {
    private int id;

    @NotBlank(message = "Tên danh mục không được để trống")
    @Size(max = 100, message = "Category name must not exceed 100 characters")
    private String name;
    private String description;
}
