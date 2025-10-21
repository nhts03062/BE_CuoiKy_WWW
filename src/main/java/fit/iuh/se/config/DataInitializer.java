package fit.iuh.se.config;

import fit.iuh.se.entities.Category;
import fit.iuh.se.entities.Product;
import fit.iuh.se.entities.UserAccount;
import fit.iuh.se.repositories.CategoryRepository;
import fit.iuh.se.repositories.ProductRepository;
import fit.iuh.se.repositories.UserAccountRepository;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initAdmin(UserAccountRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            String adminEmail = "admin@pcstore.vn";

            if (!userRepository.existsByEmail(adminEmail)) {
                UserAccount admin = new UserAccount();
                admin.setEmail(adminEmail);
                admin.setPassword(passwordEncoder.encode("admin123456"));
                admin.setFullName("Super Admin");
                admin.setIsAdmin(true);
                admin.setIsVerified(true);
                admin.setPhone("0900000000");
                admin.setAddress("Tòa nhà FPT Tân Thuận, 2A Tôn Đức Thắng, Phường Bến Nghé, Quận 1, TP.HCM");

                userRepository.save(admin);

                System.out.println("Default admin created successfully!");
                System.out.println("Email: " + adminEmail);
                System.out.println("Password: admin123456");
            } else {
                System.out.println("Admin user already exists.");
            }
        };
    }

    /**
     * Seed demo data so each main table has enough records for testing/demo.
     */
    @Bean
    CommandLineRunner seedDemoData(
            CategoryRepository categoryRepository,
            ProductRepository productRepository,
            UserAccountRepository userRepository,
            PasswordEncoder passwordEncoder
    ) {
        return args -> {
            // ---- Seed Categories (at least 10) ----
            if (categoryRepository.count() < 10) {
                List<Category> vietnameseCategories = Arrays.asList(
                        buildCategory("Máy tính để bàn",
                                "Các loại máy tính để bàn hiệu năng cao, phù hợp cho văn phòng và gaming"),
                        buildCategory("Laptop",
                                "Laptop xách tay đa dạng từ văn phòng đến gaming, thiết kế đồ họa"),
                        buildCategory("Linh kiện máy tính",
                                "CPU, RAM, Mainboard, VGA và các linh kiện máy tính khác"),
                        buildCategory("Màn hình máy tính",
                                "Màn hình LCD, LED, OLED với nhiều kích thước và độ phân giải"),
                        buildCategory("Bàn phím",
                                "Bàn phím cơ, bàn phím membrane cho game thủ và văn phòng"),
                        buildCategory("Chuột máy tính",
                                "Chuột gaming, chuột văn phòng với độ chính xác cao"),
                        buildCategory("Tai nghe",
                                "Tai nghe gaming, tai nghe văn phòng với chất lượng âm thanh tốt"),
                        buildCategory("Ổ cứng",
                                "SSD, HDD với dung lượng lưu trữ từ 256GB đến 4TB"),
                        buildCategory("Thiết bị mạng",
                                "Router, Switch, USB Wifi, Card mạng cho kết nối internet"),
                        buildCategory("Phụ kiện máy tính",
                                "Webcam, loa, đế tản nhiệt, cáp kết nối và các phụ kiện khác")
                );

                vietnameseCategories.forEach(category -> {
                    if (categoryRepository.findByName(category.getName()).isEmpty()) {
                        categoryRepository.save(category);
                    }
                });

                System.out.println("Seeded Vietnamese test categories.");
            }

            // Reload categories for product seeding
            List<Category> allCategories = categoryRepository.findAll();
            if (!allCategories.isEmpty() && productRepository.count() < 50) {
                Random random = new Random();

                for (int i = 1; i <= 50; i++) {
                    Category category = allCategories.get(random.nextInt(allCategories.size()));

                    Product product = new Product();
                    product.setName("Sản phẩm " + i);
                    product.setDescription("Mô tả chi tiết cho sản phẩm demo số " + i);
                    // Giá ngẫu nhiên từ 5.000.000 đến 50.000.000
                    long price = 5_000_000L + (long) (random.nextInt(46) * 1_000_000L);
                    product.setPrice(BigDecimal.valueOf(price));
                    product.setStock(10 + random.nextInt(90));
                    product.setImageUrl("https://via.placeholder.com/600x600?text=Product+" + i);
                    product.setIsActive(true);
                    product.setCategory(category);

                    productRepository.save(product);
                }

                System.out.println("Seeded 50 demo products.");
            }

            // ---- Seed demo customers (50 users total including existing ones) ----
            long existingUsers = userRepository.count();
            int targetUsers = 50;
            if (existingUsers < targetUsers) {
                for (int i = (int) existingUsers + 1; i <= targetUsers; i++) {
                    String email = "customer" + i + "@test.vn";
                    if (userRepository.existsByEmail(email)) {
                        continue;
                    }

                    UserAccount customer = new UserAccount();
                    customer.setEmail(email);
                    customer.setPassword(passwordEncoder.encode("customer123"));
                    customer.setFullName("Khách hàng " + i);
                    customer.setPhone("090" + String.format("%07d", i));
                    customer.setAddress("Địa chỉ demo số " + i + ", TP. Hồ Chí Minh");
                    customer.setIsAdmin(false);
                    customer.setIsVerified(true);

                    userRepository.save(customer);
                }

                System.out.println("Seeded demo customers up to " + targetUsers + " users.");
            }
        };
    }

    private Category buildCategory(String name, String description) {
        Category category = new Category();
        category.setName(name);
        category.setDescription(description);
        return category;
    }
}
