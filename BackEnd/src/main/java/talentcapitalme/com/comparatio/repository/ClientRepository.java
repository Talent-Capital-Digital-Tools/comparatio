package talentcapitalme.com.comparatio.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import talentcapitalme.com.comparatio.entity.Client;

import java.util.List;
import java.util.Optional;

public interface ClientRepository extends MongoRepository<Client, String> {
    
    Optional<Client> findByName(String name);
    
    List<Client> findByActiveTrue();
    
    boolean existsByName(String name);
}