package fit.iuh.se.services;

import fit.iuh.se.entities.Order;
import fit.iuh.se.entities.UserAccount;

public interface EmailService {
    void sendVerificationEmail(UserAccount user, String verifyLink);
    void sendOrderConfirmationEmail(Order order);
    void sendPasswordResetEmail(UserAccount user, String resetToken);
}
