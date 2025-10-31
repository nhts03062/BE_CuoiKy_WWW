package fit.iuh.se.dtos;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PromotionDTO {

    private int id;

    @NotBlank(message = "Tên khuyến mãi không được để trống")
    @Size(max = 200, message = "Tên khuyến mãi không được vượt quá 200 ký tự")
    private String name;

    @NotBlank(message = "Mã khuyến mãi là bắt buộc")
    @Pattern(regexp = "^[A-Z0-9_-]+$", message = "Mã khuyến mãi chỉ được chứa chữ in hoa, số, dấu gạch ngang hoặc gạch dưới")
    private String code;

    @NotBlank(message = "Loại giảm giá là bắt buộc (Phần trăm hoặc Cố định)")
    @Pattern(regexp = "^(PERCENT|FIXED)$", message = "Loại giảm giá chỉ hợp lệ: Phần trăm hoặc Cố định")
    private String discountType;

    @NotNull(message = "Giá trị giảm giá là bắt buộc")
    @DecimalMin(value = "0.01", message = "Giá trị giảm phải lớn hơn 0")
    private Double discountValue;

    @NotNull(message = "Ngày bắt đầu là bắt buộc")
    private LocalDate startDate;

    @NotNull(message = "Ngày kết thúc là bắt buộc")
    @FutureOrPresent(message = "Ngày kết thúc phải là hiện tại hoặc tương lai")
    private LocalDate endDate;

    @NotNull(message = "Trạng thái hoạt động là bắt buộc")
    private Boolean isActive = true;

    @Valid
    private List<PromotionDetailDTO> details;
}
