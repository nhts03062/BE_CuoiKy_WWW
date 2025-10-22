package fit.iuh.se.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserUpdateDTO {
    @Email(message = "Email không hợp lệ")
    private String email;

    @Size(min = 6, message = "Mật khẩu phải có ít nhất 6 ký tự")
    private String password;

    private String fullName;
    private String phone;
    private String address;
    private Boolean isAdmin;
    private Boolean isVerified;
    private Boolean isActive;
}

