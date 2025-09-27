package talentcapitalme.com.comparatio.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import talentcapitalme.com.comparatio.entity.JobGrade;

public interface JobGradeRepository extends MongoRepository<JobGrade, String> {}