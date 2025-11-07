package fit.iuh.se.entities;

import jakarta.persistence.*;
import lombok.*;
import java.util.List;

@Entity
@Table(name = "orders")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class Order extends BaseEntity {
   @ManyToOne(fetch = FetchType.LAZY)
   @JoinColumn(name = "user_id", nullable = false)
   private UserAccount userAccount;

   @Column(name = "order_code")
   private String orderCode;

   @Column(name = "discount_amount")
   private double discountAmount;

   @Column(name = "final_amount")
   private double finalAmount;

   @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
   private List<OrderItem> orderItems;

   @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
   private List<Payment> payments;

   @Column(name = "payment_method")
   private String paymentMethod; // "CASH", "VNPAY"

   @Column(name = "payment_status")
   private String paymentStatus; // "PENDING", "SUCCESS", "FAIL"

   private String status; // "PENDING", "CONFIRMED", "SHIPPING", "COMPLETED"

   @Column(name = "shipping_address")
   private String shippingAddress;

   private String note;
}
