package com.instalert_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(exclude=org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration.class, scanBasePackages={"com.instalert_backend.shared", "com.instalert_backend.communities", "com.instalert_backend.emergencies", "com.instalert_backend.incidents", "com.instalert_backend.transition"})
@EntityScan(basePackages={"com.instalert_backend.communities", "com.instalert_backend.emergencies", "com.instalert_backend.incidents"})
@EnableJpaRepositories(basePackages={"com.instalert_backend.communities", "com.instalert_backend.emergencies", "com.instalert_backend.incidents"})
@EnableJpaAuditing
public class InstalertBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(InstalertBackendApplication.class, args);
	}

}
