package fit.iuh.se.servicesImpl;

import fit.iuh.se.dtos.CartItemDTO;
import fit.iuh.se.dtos.OrderDTO;
import fit.iuh.se.dtos.OrderItemDTO;
import fit.iuh.se.entities.*;
import fit.iuh.se.repositories.*;
import fit.iuh.se.services.CartService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class CartServiceImpl implements CartService {

    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private UserAccountRepository userAccountRepository;
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private OrderItemRepository orderItemRepository;
    @Autowired
    private PaymentRepository paymentRepository;
    @Autowired
    private CartRepository cartRepository;
    @Autowired
    private CartItemRepository cartItemRepository;
    @Autowired
    private HttpSession session;

    private static final String SESSION_CART = "GUEST_CART";

    @Override
    public void addToCart(int productId, Integer quantity) {
        if (quantity == null || quantity <= 0) {
            throw new RuntimeException("Số lượng phải lớn hơn 0");
        }

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm ID: " + productId));

        if (product.getStock() <= 0) {
            throw new RuntimeException("Sản phẩm " + product.getName() + " đã hết hàng");
        }

        UserAccount sessionUser = (UserAccount) session.getAttribute("loggedUser");
        session.getAttributeNames().asIterator()
                .forEachRemaining(name -> System.out.println("  - " + name + ": " + session.getAttribute(name)));

        if (sessionUser != null) {
            UserAccount loggedUser = userAccountRepository.findById(sessionUser.getId())
                    .orElseThrow(() -> new RuntimeException("User không tồn tại trong DB"));

            Cart cart = cartRepository.findByUserAccount(loggedUser)
                    .orElseGet(() -> {
                        Cart newCart = new Cart();
                        newCart.setUserAccount(loggedUser);
                        newCart.setCreatedAt(LocalDateTime.now());
                        newCart.setUpdatedAt(LocalDateTime.now());
                        return cartRepository.save(newCart);
                    });

            Optional<CartItem> existingItem = cartItemRepository.findByCartAndProduct(cart, product);

            if (existingItem.isPresent()) {
                CartItem item = existingItem.get();
                int newQuantity = item.getQuantity() + quantity;
                if (newQuantity > product.getStock()) {
                    throw new RuntimeException("Không đủ hàng. Hiện chỉ còn " + product.getStock() + " sản phẩm.");
                }
                item.setQuantity(newQuantity);
                item.setSubtotal(item.getPrice() * newQuantity);
                item.setUpdatedAt(LocalDateTime.now());
                cartItemRepository.save(item);
            } else {
                if (quantity > product.getStock()) {
                    throw new RuntimeException("Không đủ hàng. Yêu cầu " + quantity + " nhưng chỉ còn "
                            + product.getStock() + " sản phẩm.");
                }

                CartItem newItem = new CartItem();
                newItem.setCart(cart);
                newItem.setProduct(product);
                newItem.setQuantity(quantity);
                newItem.setPrice(product.getPrice().doubleValue());
                newItem.setSubtotal(product.getPrice().doubleValue() * quantity);
                newItem.setCreatedAt(LocalDateTime.now());
                newItem.setUpdatedAt(LocalDateTime.now());
                newItem = cartItemRepository.save(newItem);
            }

            cart.setUpdatedAt(LocalDateTime.now());
            cartRepository.save(cart);

        }

        Map<Integer, CartItemDTO> sessionCart = getSessionCart();
        CartItemDTO cartItem = sessionCart.get(productId);
        if (cartItem != null) {
            int newQuantity = cartItem.getQuantity() + quantity;
            if (newQuantity > product.getStock()) {
                throw new RuntimeException("Không đủ số lượng trong kho. Hiện chỉ còn " + product.getStock());
            }
            cartItem.setQuantity(newQuantity);
            cartItem.setSubtotal(cartItem.getPrice() * cartItem.getQuantity());
        } else {
            if (quantity > product.getStock()) {
                throw new RuntimeException(
                        "Không đủ hàng. Yêu cầu " + quantity + " nhưng chỉ còn " + product.getStock() + " sản phẩm.");
            }

            sessionCart.put(productId, new CartItemDTO(
                    0,
                    product.getId(),
                    product.getName(),
                    product.getPrice().doubleValue(),
                    quantity,
                    product.getPrice().doubleValue() * quantity));
        }
        session.setAttribute(SESSION_CART, sessionCart);
    }

    @Override
    public void removeFromCart(int productId) {
        UserAccount sessionUser = (UserAccount) session.getAttribute("loggedUser");

        if (sessionUser != null) {
            UserAccount loggedUser = userAccountRepository.findById(sessionUser.getId())
                    .orElseThrow(() -> new RuntimeException("User không tồn tại trong DB"));

            Optional<Cart> cartOpt = cartRepository.findByUserAccount(loggedUser);
            if (cartOpt.isPresent()) {
                Cart cart = cartOpt.get();
                Product product = productRepository.findById(productId)
                        .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm ID: " + productId));

                Optional<CartItem> cartItemOpt = cartItemRepository.findByCartAndProduct(cart, product);
                if (cartItemOpt.isPresent()) {
                    cartItemRepository.delete(cartItemOpt.get());
                    cart.setUpdatedAt(LocalDateTime.now());
                    cartRepository.save(cart);
                }
            }
        }

        Map<Integer, CartItemDTO> cart = getSessionCart();
        cart.remove(productId);
        session.setAttribute(SESSION_CART, cart);
    }

    @Override
    public void updateCartItem(int productId, Integer quantity) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm ID: " + productId));

        if (quantity <= 0) {
            removeFromCart(productId);
            return;
        }
        if (quantity > product.getStock()) {
            throw new RuntimeException("Sản phẩm " + product.getName() + " đã hết hàng.");
        }

        UserAccount sessionUser = (UserAccount) session.getAttribute("loggedUser");

        if (sessionUser != null) {
            UserAccount loggedUser = userAccountRepository.findById(sessionUser.getId())
                    .orElseThrow(() -> new RuntimeException("User không tồn tại trong DB"));

            Optional<Cart> cartOpt = cartRepository.findByUserAccount(loggedUser);
            if (cartOpt.isPresent()) {
                Cart cart = cartOpt.get();
                Optional<CartItem> cartItemOpt = cartItemRepository.findByCartAndProduct(cart, product);

                if (cartItemOpt.isPresent()) {
                    CartItem cartItem = cartItemOpt.get();
                    cartItem.setQuantity(quantity);
                    cartItem.setSubtotal(cartItem.getPrice() * quantity);
                    cartItem.setUpdatedAt(LocalDateTime.now());
                    cartItemRepository.save(cartItem);

                    cart.setUpdatedAt(LocalDateTime.now());
                    cartRepository.save(cart);
                }
            }
        }

        Map<Integer, CartItemDTO> cart = getSessionCart();
        CartItemDTO item = cart.get(productId);
        if (item != null) {
            item.setQuantity(quantity);
            item.setSubtotal(item.getPrice() * quantity);
            session.setAttribute(SESSION_CART, cart);
        } else {
            throw new RuntimeException("Sản phẩm không có trong giỏ hàng");
        }
    }

    @Override
    public void clearCart() {
        UserAccount sessionUser = (UserAccount) session.getAttribute("loggedUser");

        if (sessionUser != null) {
            UserAccount loggedUser = userAccountRepository.findById(sessionUser.getId())
                    .orElseThrow(() -> new RuntimeException("User không tồn tại trong DB"));

            Optional<Cart> cartOpt = cartRepository.findByUserAccount(loggedUser);
            if (cartOpt.isPresent()) {
                Cart cart = cartOpt.get();
                cartItemRepository.deleteAllByCart(cart);
            }
        }
        session.removeAttribute(SESSION_CART);
    }

    @Override
    public List<CartItemDTO> getCartItems() {
        try {
            UserAccount loggedUser = (UserAccount) session.getAttribute("loggedUser");

            if (loggedUser != null) {
                UserAccount user = userAccountRepository.findById(loggedUser.getId())
                        .orElseThrow(() -> new RuntimeException("User không tồn tại trong DB"));

                Optional<Cart> cartOpt = cartRepository.findByUserAccount(user);
                if (cartOpt.isPresent()) {
                    Cart cart = cartOpt.get();
                    List<CartItem> cartItems = cartItemRepository.findByCart(cart);

                    List<CartItemDTO> result = cartItems.stream()
                            .map(item -> new CartItemDTO(
                                    item.getId(),
                                    item.getProduct().getId(),
                                    item.getProduct().getName(),
                                    item.getPrice(),
                                    item.getQuantity(),
                                    item.getSubtotal()))
                            .collect(Collectors.toList());

                    return result;
                } else {
                    return new ArrayList<>();
                }
            } else {
                Map<Integer, CartItemDTO> sessionCart = getSessionCart();
                List<CartItemDTO> result = new ArrayList<>(sessionCart.values());
                return result;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    @Override
    public double getCartTotal() {
        return getCartItems().stream().mapToDouble(CartItemDTO::getSubtotal).sum();
    }

    @Override
    public int getCartItemCount() {
        return getCartItems().stream().mapToInt(CartItemDTO::getQuantity).sum();
    }

    @Override
    public boolean isCartEmpty() {
        return getCartItems().isEmpty();
    }

    @Override
    @Transactional
    public OrderDTO checkout(int userId, String paymentMethod, String shippingAddress, String note) {

        if (paymentMethod == null || paymentMethod.trim().isEmpty()) {
            throw new RuntimeException("Phương thức thanh toán không được để trống");
        }
        if (shippingAddress == null || shippingAddress.trim().isEmpty()) {
            throw new RuntimeException("Địa chỉ giao hàng không được để trống");
        }

        if (note == null) {
            note = "";
        }

        UserAccount user = userAccountRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng ID: " + userId));

        Map<Integer, CartItemDTO> sessionCart = getSessionCart();
        if (sessionCart.isEmpty()) {
            throw new RuntimeException("Giỏ hàng trống, không thể thanh toán!");
        }

        Cart userCart = cartRepository.findByUserAccount(user)
                .orElseGet(() -> {
                    Cart newCart = new Cart();
                    newCart.setUserAccount(user);
                    newCart.setCreatedAt(LocalDateTime.now());
                    newCart.setUpdatedAt(LocalDateTime.now());
                    return cartRepository.save(newCart);
                });

        List<CartItem> cartItemsForHistory = new ArrayList<>();
        for (CartItemDTO sessionItem : sessionCart.values()) {
            Product product = productRepository.findById(sessionItem.getProductId())
                    .orElseThrow(() -> new RuntimeException("Sản phẩm không tồn tại: " + sessionItem.getProductName()));

            CartItem cartItem = new CartItem();
            cartItem.setCart(userCart);
            cartItem.setProduct(product);
            cartItem.setQuantity(sessionItem.getQuantity());
            cartItem.setPrice(sessionItem.getPrice());
            cartItem.setSubtotal(sessionItem.getSubtotal());
            cartItem.setCreatedAt(LocalDateTime.now());
            cartItem.setUpdatedAt(LocalDateTime.now());
            cartItemsForHistory.add(cartItem);
        }
        cartItemRepository.saveAll(cartItemsForHistory);

        userCart.setUpdatedAt(LocalDateTime.now());
        cartRepository.save(userCart);

        Order order = new Order();
        order.setUserAccount(user);
        order.setOrderCode("ORDER_" + System.currentTimeMillis());
        order.setShippingAddress(shippingAddress);
        order.setNote(note);
        order.setStatus("PENDING");
        order.setPaymentMethod(paymentMethod);
        order.setPaymentStatus("PENDING");
        double totalAmount = sessionCart.values().stream().mapToDouble(CartItemDTO::getSubtotal).sum();
        order.setFinalAmount(totalAmount);
        order.setDiscountAmount(0.0);
        order.setCreatedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());

        Order savedOrder = orderRepository.save(order);

        List<OrderItem> orderItems = new ArrayList<>();
        for (CartItemDTO cartItemDTO : sessionCart.values()) {
            Product product = productRepository.findById(cartItemDTO.getProductId())
                    .orElseThrow(
                            () -> new RuntimeException("Sản phẩm không tồn tại: " + cartItemDTO.getProductName()));

            if (cartItemDTO.getQuantity() > product.getStock()) {
                throw new RuntimeException("Không đủ hàng cho sản phẩm: " + product.getName() +
                        ". Yêu cầu: " + cartItemDTO.getQuantity() +
                        ", Còn lại: " + product.getStock());
            }
            product.setStock(product.getStock() - cartItemDTO.getQuantity());
            productRepository.save(product);

            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(savedOrder);
            orderItem.setProduct(product);
            orderItem.setQuantity(cartItemDTO.getQuantity());
            orderItem.setPrice(cartItemDTO.getPrice());
            orderItem.setSubTotal(cartItemDTO.getSubtotal());
            orderItems.add(orderItem);
        }
        orderItemRepository.saveAll(orderItems);
        savedOrder.setOrderItems(orderItems);

        Payment payment = new Payment();
        payment.setOrder(savedOrder);
        payment.setUserAccount(user);
        payment.setMethod(paymentMethod);
        payment.setAmount(totalAmount);
        payment.setStatus(paymentMethod.equalsIgnoreCase("VNPAY") ? "PENDING" : "SUCCESS");
        payment.setPaymenyDate(LocalDateTime.now());
        payment.setTransactionRef("TXN_" + System.currentTimeMillis());
        paymentRepository.save(payment);

        userCart.getItems().clear();
        userCart.setUpdatedAt(LocalDateTime.now());
        cartRepository.save(userCart);

        session.removeAttribute(SESSION_CART);

        return convertToDTO(savedOrder);
    }

    @SuppressWarnings("unchecked")
    private Map<Integer, CartItemDTO> getSessionCart() {
        Map<Integer, CartItemDTO> cart = (Map<Integer, CartItemDTO>) session.getAttribute(SESSION_CART);
        if (cart == null) {
            cart = new HashMap<>();
            session.setAttribute(SESSION_CART, cart);
        }
        return cart;
    }

    private OrderDTO convertToDTO(Order order) {
        OrderDTO orderDTO = new OrderDTO();
        orderDTO.setId(order.getId());
        orderDTO.setUserId(order.getUserAccount() != null ? order.getUserAccount().getId() : 0);
        orderDTO.setOrderCode(order.getOrderCode() != null ? order.getOrderCode() : "");
        orderDTO.setDiscountAmount(order.getDiscountAmount());
        orderDTO.setFinalAmount(order.getFinalAmount());
        orderDTO.setPaymentMethod(order.getPaymentMethod() != null ? order.getPaymentMethod() : "");
        orderDTO.setPaymentStatus(order.getPaymentStatus() != null ? order.getPaymentStatus() : "");
        orderDTO.setStatus(order.getStatus() != null ? order.getStatus() : "");
        orderDTO.setShippingAddress(order.getShippingAddress() != null ? order.getShippingAddress() : "");
        orderDTO.setNote(order.getNote() != null ? order.getNote() : "");
        orderDTO.setCreatedAt(order.getCreatedAt());

        if (order.getOrderItems() != null) {
            orderDTO.setItems(order.getOrderItems().stream().map(this::convertOrderItemToDTO).toList());
        }
        return orderDTO;
    }

    private OrderItemDTO convertOrderItemToDTO(OrderItem orderItem) {
        return new OrderItemDTO(
                orderItem.getId(),
                orderItem.getProduct().getId(),
                orderItem.getProduct().getName(),
                orderItem.getQuantity(),
                orderItem.getPrice(),
                orderItem.getSubTotal());
    }
}