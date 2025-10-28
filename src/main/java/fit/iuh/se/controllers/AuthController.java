package fit.iuh.se.controllers;

import fit.iuh.se.dtos.LoginRequestDTO;
import fit.iuh.se.dtos.RegisterRequest;
import fit.iuh.se.services.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/register")
    @ResponseBody
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest dto) {
        return authService.register(dto);
    }

    @GetMapping("/verify")
    @ResponseBody
    public String verifyEmail(@RequestParam("token") String token) {
        ResponseEntity<?> response = authService.verifyEmail(token);

        if (response.getStatusCode().is2xxSuccessful()) {
            return """
                        <script>
                            alert('✅ Tài khoản của bạn đã được xác thực thành công!');
                            window.location.href = 'http://localhost:3000/login';
                        </script>
                    """;
        }

        return """
                    <script>
                        alert('❌ Liên kết xác thực không hợp lệ hoặc đã hết hạn!');
                        window.location.href = 'http://localhost:3000/register';
                    </script>
                """;
    }

    @PostMapping("/login")
    @ResponseBody
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequestDTO request) {
        return authService.login(request);
    }

    @GetMapping("/user/profile")
    @ResponseBody
    public ResponseEntity<?> getProfile(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "").trim();
        return authService.getProfileByToken(token);
    }
}
