package fit.iuh.se.services;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import fit.iuh.se.dtos.UserCreateDTO;
import fit.iuh.se.dtos.UserDTO;
import fit.iuh.se.dtos.UserUpdateDTO;

public interface UserService {
    Page<UserDTO> findAll(Pageable pageable);

    UserDTO findById(Integer id);

    UserDTO create(UserCreateDTO dto);

    UserDTO update(Integer id, UserUpdateDTO dto);

    void delete(Integer id);

    UserDTO activate(Integer id);

    UserDTO deactivate(Integer id);
}

