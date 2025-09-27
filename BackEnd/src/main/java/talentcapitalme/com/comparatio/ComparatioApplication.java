package talentcapitalme.com.comparatio;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ComparatioApplication {

	public static void main(String[] args) {
		SpringApplication.run(ComparatioApplication.class, args);
	}

}
