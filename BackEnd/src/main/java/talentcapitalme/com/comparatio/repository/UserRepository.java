package talentcapitalme.com.comparatio.repository;



import org.springframework.data.mongodb.repository.MongoRepository;
import talentcapitalme.com.comparatio.entity.User;

import java.util.Optional;

public interface UserRepository extends MongoRepository<User, String> {
    Optional<User> findByEmail(String email);

}