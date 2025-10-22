package fit.iuh.se.dtos;

import lombok.Data;

@Data
public class UserDTO {
    private Integer id;
    private String email;
    private String fullName;
    private String phone;
    private String address;
    private Boolean isAdmin;
    private Boolean isVerified;
    private Boolean isActive;
}

