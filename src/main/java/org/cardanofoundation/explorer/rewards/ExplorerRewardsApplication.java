package org.cardanofoundation.explorer.rewards;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@EnableConfigurationProperties
@EntityScan({"org.cardanofoundation.explorer.*", "org.cardanofoundation.*"})
@SpringBootApplication
public class ExplorerRewardsApplication {

  public static void main(String[] args) {
    SpringApplication.run(ExplorerRewardsApplication.class, args);
  }
}
