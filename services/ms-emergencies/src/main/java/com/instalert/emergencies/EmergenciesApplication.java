package com.instalert.emergencies;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
@SpringBootApplication(exclude=org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration.class)
public class EmergenciesApplication {
 public static void main(String[] args) { SpringApplication.run(EmergenciesApplication.class,args); }
}
