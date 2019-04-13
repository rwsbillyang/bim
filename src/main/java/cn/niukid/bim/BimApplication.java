package cn.niukid.bim;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

// mvn spring-boot:run
// java -jar target/bim-0.0.1-SNAPSHOT.jar
// java -Xdebug -Xrunjdwp:server=y,transport=dt_socket,address=8000,suspend=n -jar target/bim-0.0.1-SNAPSHOT.jar
@SpringBootApplication
@EnableScheduling
//@EnableAsync
public class BimApplication {

	public static void main(String[] args) {
		SpringApplication.run(BimApplication.class, args);
	}
}
