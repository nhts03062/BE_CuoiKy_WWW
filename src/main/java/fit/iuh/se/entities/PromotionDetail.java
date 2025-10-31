package fit.iuh.se.entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "promotion_detail")
@Data
@NoArgsConstructor
public class PromotionDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne
    @JoinColumn(name = "promotion_id", nullable = false)
    private PromotionBase promotion;

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "min_order_value")
    private Double minOrderValue;
}
