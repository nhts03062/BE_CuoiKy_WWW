package fit.iuh.se.services;

import java.util.List;

import fit.iuh.se.dtos.CartItemDTO;
import fit.iuh.se.dtos.OrderDTO;

public interface CartService {
    void addToCart(int productId, Integer quantity);

    void removeFromCart(int productId);

    void updateCartItem(int productId, Integer quantity);

    void clearCart();

    List<CartItemDTO> getCartItems();

    double getCartTotal();

    int getCartItemCount();

    boolean isCartEmpty();

    OrderDTO checkout(int userId, String paymentMethod, String shippingAddress, String note);
}