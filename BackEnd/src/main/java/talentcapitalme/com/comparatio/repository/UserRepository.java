package talentcapitalme.com.comparatio.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import talentcapitalme.com.comparatio.entity.User;
import talentcapitalme.com.comparatio.enumeration.UserRole;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends MongoRepository<User, String> {
    Optional<User> findByEmail(String email);
    
    // Methods for CLIENT_ADMIN users (replacing Client functionality)
    Optional<User> findByName(String name);
    List<User> findByActiveTrue();
    List<User> findByRoleAndActiveTrue(UserRole role);
    boolean existsByName(String name);
}