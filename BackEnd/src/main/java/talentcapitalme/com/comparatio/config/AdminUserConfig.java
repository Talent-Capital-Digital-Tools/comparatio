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
        return userRepository.findByEmail("admin@talentcapital.com")
                .orElseGet(() -> {
                    User admin = new User();
                    admin.setEmail("admin@talentcapital.com");
                    admin.setUsername("admin");
                    admin.setFullName("Talent Capital Administrator");
                    admin.setPasswordHash(passwordEncoder.encode("admin"));
                    admin.setRole(UserRole.SUPER_ADMIN);
                    admin.setName("Talent Capital");
                    admin.setIndustry("Technology");
                    admin.setActive(true);
                    // Super admin with Talent Capital company details
                    return userRepository.save(admin);
                });
    }
}
