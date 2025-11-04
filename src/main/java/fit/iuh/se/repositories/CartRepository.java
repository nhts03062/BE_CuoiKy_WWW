package fit.iuh.se.repositories;

import fit.iuh.se.entities.Cart;
import fit.iuh.se.entities.UserAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<Cart, Integer> {
    Optional<Cart> findByUserAccountId(int userId);
    Optional<Cart> findByUserAccount(UserAccount userAccount);
}