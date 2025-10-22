package fit.iuh.se.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserCreateDTO {
    @NotBlank(message = "Email là bắt buộc")
    @Email(message = "Email không hợp lệ")
    private String email;

    @NotBlank(message = "Mật khẩu là bắt buộc")
    @Size(min = 6, message = "Mật khẩu phải có ít nhất 6 ký tự")
    private String password;

    @NotBlank(message = "Họ tên là bắt buộc")
    private String fullName;

    private String phone;
    private String address;
    private Boolean isAdmin = false;
    private Boolean isVerified = false;
    private Boolean isActive = true;
}

