package fit.iuh.se.servicesImpl;

import fit.iuh.se.entities.Order;
import fit.iuh.se.entities.UserAccount;
import fit.iuh.se.services.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class EmailServiceImpl implements EmailService {
    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Override
    public void sendVerificationEmail(UserAccount user, String verifyLink) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(user.getEmail());
            message.setSubject("Xác thực tài khoản - PC Store");

            String emailBody = String.format(
                    "Xin chào %s,\n\n" +
                            "Cảm ơn bạn đã đăng ký tại PC Store.\n\n" +
                            "Vui lòng nhấn vào liên kết dưới đây để xác thực tài khoản của bạn:\n%s\n\n" +
                            "Liên kết này sẽ hết hạn sau 24 giờ.\n\n" +
                            "Nếu bạn không đăng ký tài khoản này, vui lòng bỏ qua email này.\n\n" +
                            "Trân trọng,\nPC Store",
                    user.getFullName() != null ? user.getFullName() : "bạn",
                    verifyLink
            );

            message.setText(emailBody);
            mailSender.send(message);

            System.out.println("Verification email sent to " + user.getEmail());
        } catch (Exception e) {
            System.err.println("Failed to send verification email: " + e.getMessage());
        }
    }

    @Override
    public void sendOrderConfirmationEmail(Order order) {}
    @Override
    public void sendPasswordResetEmail(UserAccount user, String resetToken) {}
}
