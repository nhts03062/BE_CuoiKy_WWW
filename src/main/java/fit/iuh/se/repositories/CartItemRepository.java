package fit.iuh.se.repositories;

import fit.iuh.se.entities.Cart;
import fit.iuh.se.entities.CartItem;
import fit.iuh.se.entities.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Integer> {
    List<CartItem> findByCart(Cart cart);

    Optional<CartItem> findByCartAndProduct(Cart cart, Product product);

    @Transactional
    @Modifying
    void deleteByCartAndProductId(Cart cart, int productId);

    @Transactional
    @Modifying
    void deleteAllByCart(Cart cart);
}