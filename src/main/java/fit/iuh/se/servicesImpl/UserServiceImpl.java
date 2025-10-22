package fit.iuh.se.servicesImpl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import fit.iuh.se.dtos.UserCreateDTO;
import fit.iuh.se.dtos.UserDTO;
import fit.iuh.se.dtos.UserUpdateDTO;
import fit.iuh.se.entities.UserAccount;
import fit.iuh.se.repositories.UserAccountRepository;
import fit.iuh.se.services.UserService;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserAccountRepository userRepo;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public Page<UserDTO> findAll(Pageable pageable) {
        return userRepo.findAll(pageable).map(this::toDto);
    }

    @Override
    public UserDTO findById(Integer id) {
        UserAccount user = userRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng!"));
        return toDto(user);
    }

    @Override
    public UserDTO create(UserCreateDTO dto) {
        if (userRepo.existsByEmail(dto.getEmail())) {
            throw new RuntimeException("Email đã tồn tại!");
        }
        UserAccount user = new UserAccount();
        user.setEmail(dto.getEmail());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setFullName(dto.getFullName());
        user.setPhone(dto.getPhone());
        user.setAddress(dto.getAddress());
        user.setIsAdmin(Boolean.TRUE.equals(dto.getIsAdmin()));
        user.setIsVerified(Boolean.TRUE.equals(dto.getIsVerified()));
        user.setIsActive(dto.getIsActive() == null ? true : dto.getIsActive());

        userRepo.save(user);
        return toDto(user);
    }

    @Override
    public UserDTO update(Integer id, UserUpdateDTO dto) {
        UserAccount user = userRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng!"));

        if (StringUtils.hasText(dto.getEmail()) && !dto.getEmail().equals(user.getEmail())) {
            if (userRepo.existsByEmail(dto.getEmail())) {
                throw new RuntimeException("Email đã tồn tại!");
            }
            user.setEmail(dto.getEmail());
        }

        if (StringUtils.hasText(dto.getPassword())) {
            user.setPassword(passwordEncoder.encode(dto.getPassword()));
        }

        if (dto.getFullName() != null) user.setFullName(dto.getFullName());
        if (dto.getPhone() != null) user.setPhone(dto.getPhone());
        if (dto.getAddress() != null) user.setAddress(dto.getAddress());
        if (dto.getIsAdmin() != null) user.setIsAdmin(dto.getIsAdmin());
        if (dto.getIsVerified() != null) user.setIsVerified(dto.getIsVerified());
        if (dto.getIsActive() != null) user.setIsActive(dto.getIsActive());

        userRepo.save(user);
        return toDto(user);
    }

    @Override
    public void delete(Integer id) {
        if (!userRepo.existsById(id)) {
            throw new RuntimeException("Không tìm thấy người dùng!");
        }
        userRepo.deleteById(id);
    }

    @Override
    public UserDTO activate(Integer id) {
        return toggleActive(id, true);
    }

    @Override
    public UserDTO deactivate(Integer id) {
        return toggleActive(id, false);
    }

    private UserDTO toggleActive(Integer id, boolean active) {
        UserAccount user = userRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng!"));
        user.setIsActive(active);
        userRepo.save(user);
        return toDto(user);
    }

    private UserDTO toDto(UserAccount user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setEmail(user.getEmail());
        dto.setFullName(user.getFullName());
        dto.setPhone(user.getPhone());
        dto.setAddress(user.getAddress());
        dto.setIsAdmin(user.getIsAdmin());
        dto.setIsVerified(user.getIsVerified());
        dto.setIsActive(user.getIsActive());
        return dto;
    }
}

