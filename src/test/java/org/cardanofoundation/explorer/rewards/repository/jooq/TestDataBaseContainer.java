package org.cardanofoundation.explorer.rewards.repository.jooq;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
public class TestDataBaseContainer {

  public static PostgreSQLContainer<?> postgresContainer;

  static {
    postgresContainer = new PostgreSQLContainer<>("postgres:14.5");
    postgresContainer.start();
  }

  @DynamicPropertySource
  public static void containersProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.username", postgresContainer::getUsername);
    registry.add("spring.datasource.password", postgresContainer::getPassword);
    registry.add("spring.datasource.url", postgresContainer::getJdbcUrl);
  }
}
