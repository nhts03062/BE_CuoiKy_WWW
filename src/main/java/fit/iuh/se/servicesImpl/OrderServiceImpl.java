package fit.iuh.se.servicesImpl;

import fit.iuh.se.dtos.OrderDTO;
import fit.iuh.se.dtos.OrderItemDTO;
import fit.iuh.se.entities.*;
import fit.iuh.se.enums.OrderStatus;
import fit.iuh.se.enums.PaymentStatus;
import fit.iuh.se.repositories.*;
import fit.iuh.se.services.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class OrderServiceImpl implements OrderService {
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private UserAccountRepository userAccountRepository;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private PaymentRepository paymentRepository;

    @Override
    public List<OrderDTO> findAll() {
        return orderRepository.findAll()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<OrderDTO> findByUserId(int userId) {
        return orderRepository.findByUserAccountId(userId)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public OrderDTO findById(int id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng ID: " + id));
        return toDTO(order);
    }

    @Override
    public OrderDTO create(OrderDTO dto) {
        UserAccount user = userAccountRepository.findById(dto.getUserId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng ID: " + dto.getUserId()));

        Order order = new Order();
        order.setUserAccount(user);
        order.setOrderCode(UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        order.setPaymentMethod(dto.getPaymentMethod());
        order.setShippingAddress(dto.getShippingAddress());
        order.setNote(dto.getNote());

        if ("CASH".equalsIgnoreCase(dto.getPaymentMethod())) {
            order.setPaymentStatus(PaymentStatus.SUCCESS.name());
            order.setStatus(OrderStatus.COMPLETED.name());
        } else {
            order.setPaymentStatus(PaymentStatus.PENDING.name());
            order.setStatus(OrderStatus.PENDING.name());
        }

        List<OrderItem> items = dto.getItems().stream().map(i -> {
            Product p = productRepository.findById(i.getProductId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm ID: " + i.getProductId()));
            OrderItem oi = new OrderItem();
            oi.setOrder(order);
            oi.setProduct(p);
            oi.setQuantity(i.getQuantity());
            oi.setPrice(p.getPrice().doubleValue());
            oi.setSubTotal(p.getPrice().doubleValue() * i.getQuantity());
            return oi;
        }).collect(Collectors.toList());

        double total = items.stream().mapToDouble(OrderItem::getSubTotal).sum();
        order.setOrderItems(items);
        order.setFinalAmount(total);
        order.setDiscountAmount(dto.getDiscountAmount() != null ? dto.getDiscountAmount() : 0);

        Order saved = orderRepository.save(order);

        if ("CASH".equalsIgnoreCase(dto.getPaymentMethod())) {
            Payment payment = new Payment();
            payment.setOrder(saved);
            payment.setUserAccount(user);
            payment.setAmount(saved.getFinalAmount());
            payment.setMethod("CASH");
            payment.setStatus(PaymentStatus.SUCCESS.name());
            payment.setPaymenyDate(LocalDateTime.now());
            paymentRepository.save(payment);
        } else {
            Payment payment = new Payment();
            payment.setOrder(saved);
            payment.setUserAccount(user);
            payment.setUserAccount(user);
            payment.setAmount(saved.getFinalAmount());
            payment.setMethod("VNPAY");
            payment.setStatus(PaymentStatus.PENDING.name());
            payment.setPaymenyDate(LocalDateTime.now());
            paymentRepository.save(payment);
        }

        return toDTO(saved);
    }

    @Override
    public OrderDTO updateStatus(int id, String status) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng ID: " + id));
        order.setStatus(status);
        return toDTO(orderRepository.save(order));
    }

    @Override
    public void delete(int id) {
        orderRepository.deleteById(id);
    }

    @Override
    public OrderDTO findEntityById(int id) {
        return toDTO(orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng ID: " + id)));
    }

    private OrderDTO toDTO(Order o) {
        return OrderDTO.builder()
                .id(o.getId())
                .userId(o.getUserAccount().getId())
                .orderCode(o.getOrderCode())
                .discountAmount(o.getDiscountAmount())
                .finalAmount(o.getFinalAmount())
                .paymentMethod(o.getPaymentMethod())
                .paymentStatus(o.getPaymentStatus())
                .status(o.getStatus())
                .shippingAddress(o.getShippingAddress())
                .note(o.getNote())
                .createdAt(o.getCreatedAt())
                .items(o.getOrderItems() != null
                        ? o.getOrderItems().stream().map(this::toItemDTO).collect(Collectors.toList())
                        : null)
                .build();
    }

    private OrderItemDTO toItemDTO(OrderItem item) {
        return OrderItemDTO.builder()
                .id(item.getId())
                .productId(item.getProduct().getId())
                .productName(item.getProduct().getName())
                .quantity(item.getQuantity())
                .price(item.getPrice())
                .subTotal(item.getSubTotal())
                .build();
    }
}
