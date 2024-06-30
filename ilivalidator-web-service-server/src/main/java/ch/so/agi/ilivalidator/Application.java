package ch.so.agi.ilivalidator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
//@ServletComponentScan
@Configuration
@EnableScheduling
public class Application {
  
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }  
}
