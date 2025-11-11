package fit.iuh.se.jobs;

import fit.iuh.se.entities.Order;
import fit.iuh.se.repositories.OrderRepository;
import fit.iuh.se.enums.OrderStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class OrderAutoCompleteJob {

    @Autowired
    private OrderRepository orderRepository;

    @Scheduled(fixedRate = 3600000)
    public void autoCompleteOrders() {
        LocalDateTime now = LocalDateTime.now();
        List<Order> shippingOrders = orderRepository.findByStatus(OrderStatus.SHIPPING.name());

        for (Order order : shippingOrders) {
            if (order.getUpdatedAt() != null && order.getUpdatedAt().isBefore(now.minusHours(24))) {
                order.setStatus(OrderStatus.COMPLETED.name());
                orderRepository.save(order);
                System.out.println("Đã tự động cập nhật đơn hàng " + order.getOrderCode() + " -> COMPLETED");
            }
        }
    }
}
