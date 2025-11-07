package fit.iuh.se.repositories;

import fit.iuh.se.entities.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Integer> {
    List<Order> findByUserAccountId(int userId);
    Optional<Order> findByOrderCode(String orderCode);
    List<Order> findByStatus(String status);
}