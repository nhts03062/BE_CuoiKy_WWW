package fit.iuh.se.servicesImpl;

import fit.iuh.se.dtos.LoginRequestDTO;
import fit.iuh.se.dtos.RegisterRequest;
import fit.iuh.se.entities.UserAccount;
import fit.iuh.se.repositories.UserAccountRepository;
import fit.iuh.se.security.JwtUtils;
import fit.iuh.se.services.AuthService;
import fit.iuh.se.services.EmailService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class AuthServiceImpl implements AuthService {

    @Autowired
    private UserAccountRepository userRepo;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EmailService emailService;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private HttpSession session;

    @Override
    public ResponseEntity<?> register(RegisterRequest req) {
        if (userRepo.existsByEmail(req.getEmail())) {
            return ResponseEntity.badRequest().body("Email đã tồn tại!");
        }

        UserAccount user = new UserAccount();
        user.setEmail(req.getEmail());
        user.setPassword(passwordEncoder.encode(req.getPassword()));
        user.setFullName(req.getFullName());
        user.setPhone(req.getPhone());
        user.setIsAdmin(false);
        user.setIsVerified(false);
        user.setIsActive(true);
        userRepo.save(user);

        String verifyToken = jwtUtils.generateToken(req.getEmail(), false);
        String verifyLink = "http://localhost:8080/api/auth/verify?token=" + verifyToken;
        emailService.sendVerificationEmail(user, verifyLink);

        return ResponseEntity.ok("Đăng ký thành công! Vui lòng kiểm tra email để xác thực tài khoản.");
    }

    @Override
    public ResponseEntity<?> login(LoginRequestDTO req) {
        Optional<UserAccount> optionalUser = userRepo.findByEmail(req.getEmail());
        if (optionalUser.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Tài khoản không tồn tại!"));
        }

        UserAccount user = optionalUser.get();

        if (!passwordEncoder.matches(req.getPassword(), user.getPassword())) {
            return ResponseEntity.badRequest().body(Map.of("error", "Sai mật khẩu!"));
        }

        if (!Boolean.TRUE.equals(user.getIsVerified())) {
            return ResponseEntity.badRequest().body(Map.of("error", "Vui lòng xác thực email trước khi đăng nhập!"));
        }

        String token = jwtUtils.generateToken(user.getEmail(), user.getIsAdmin());

        session.setAttribute("loggedUser", user);
        System.out.println("Lưu loggedUser vào session: " + user.getEmail());

        Map<String, Object> response = new HashMap<>();
        response.put("token", token);
        response.put("role", user.getIsAdmin() ? "ADMIN" : "USER");
        response.put("user", Map.ofEntries(
                Map.entry("id", user.getId()),
                Map.entry("email", user.getEmail()),
                Map.entry("fullName", user.getFullName()),
                Map.entry("phone", user.getPhone()),
                Map.entry("address", user.getAddress() == null ? "" : user.getAddress()),
                Map.entry("isAdmin", user.getIsAdmin()),
                Map.entry("isVerified", user.getIsVerified())
        ));

        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<?> verifyEmail(String token) {
        if (!jwtUtils.validateToken(token)) {
            return ResponseEntity.badRequest().body("Liên kết xác thực không hợp lệ hoặc đã hết hạn!");
        }

        String email = jwtUtils.extractEmail(token);
        Optional<UserAccount> optionalUser = userRepo.findByEmail(email);
        if (optionalUser.isEmpty()) {
            return ResponseEntity.badRequest().body("Không tìm thấy người dùng!");
        }

        UserAccount user = optionalUser.get();
        user.setIsVerified(true);
        userRepo.save(user);

        return ResponseEntity.ok("Tài khoản của bạn đã được xác thực thành công!");
    }

    @Override
    public ResponseEntity<?> getProfileByToken(String token) {
        if (!jwtUtils.validateToken(token)) {
            return ResponseEntity.badRequest().body(Map.of("error", "Token không hợp lệ hoặc đã hết hạn!"));
        }

        String email = jwtUtils.extractEmail(token);
        Optional<UserAccount> optionalUser = userRepo.findByEmail(email);

        if (optionalUser.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Không tìm thấy người dùng!"));
        }

        UserAccount user = optionalUser.get();

        Map<String, Object> response = new HashMap<>();
        response.put("id", user.getId());
        response.put("email", user.getEmail());
        response.put("fullName", user.getFullName());
        response.put("phone", user.getPhone());
        response.put("address", user.getAddress());
        response.put("isAdmin", user.getIsAdmin());
        response.put("isVerified", user.getIsVerified());
        response.put("role", user.getIsAdmin() ? "ADMIN" : "USER");

        return ResponseEntity.ok(response);
    }
}
