package talentcapitalme.com.comparatio.config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import talentcapitalme.com.comparatio.entity.User;
import talentcapitalme.com.comparatio.enumeration.UserRole;
import talentcapitalme.com.comparatio.repository.UserRepository;

@Configuration
public class AdminUserConfig {


    @Bean
    public User adminUser(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return userRepository.findByEmail("admin@gmail.com")
                .orElseGet(() -> {
                    User admin = new User();
                    admin.setId("admin-001");

                    admin.setEmail("admin@gmail.com");
                    admin.setUsername("admin");
                    admin.setFullName("System Administrator");
                    admin.setPasswordHash(passwordEncoder.encode("admin"));
                    admin.setRole(UserRole.SUPER_ADMIN);
                    // Super admin has no client restriction (no name/active fields needed)
                    return userRepository.save(admin);
                });
    }
}
