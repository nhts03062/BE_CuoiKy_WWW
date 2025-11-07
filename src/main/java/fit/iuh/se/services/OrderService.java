package fit.iuh.se.services;

import fit.iuh.se.dtos.OrderDTO;

import java.util.List;

public interface OrderService {
    List<OrderDTO> findAll();
    List<OrderDTO> findByUserId(int userId);
    OrderDTO findById(int id);
    OrderDTO create(OrderDTO dto);
    OrderDTO updateStatus(int id, String status);
    void delete(int id);
    OrderDTO findEntityById(int id);
}