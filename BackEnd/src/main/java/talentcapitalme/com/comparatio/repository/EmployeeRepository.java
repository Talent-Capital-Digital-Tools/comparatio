package talentcapitalme.com.comparatio.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import talentcapitalme.com.comparatio.entity.Employee;

public interface EmployeeRepository extends MongoRepository<Employee, String> {}