package sgab.sgab;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class SgabApplication {

    public static void main(String[] args) {
        SpringApplication.run(SgabApplication.class, args);
    }
}
