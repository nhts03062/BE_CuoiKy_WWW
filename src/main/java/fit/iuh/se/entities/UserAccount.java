package fit.iuh.se.entities;

import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "user_accounts")
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = { "orders", "cart", "payments" })
public class UserAccount extends BaseEntity {
    @NotBlank(message = "Username is required")
    private String email;
    @NotBlank(message = "Password is required")
    private String password;
    @Column(name = "full_name")
    private String fullName;
    private String phone;
    private String address;
    @Column(name = "is_admin")
    private Boolean isAdmin;
    @Column(name = "is_verified")
    private Boolean isVerified;
    @Column(name = "is_active")
    private Boolean isActive = true;

    @OneToMany(mappedBy = "userAccount", orphanRemoval = true, cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<Order> orders;
    @OneToOne(mappedBy = "userAccount", cascade = CascadeType.ALL)
    private Cart cart;
    @OneToMany(mappedBy = "userAccount", orphanRemoval = true, cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Payment> payments;
}