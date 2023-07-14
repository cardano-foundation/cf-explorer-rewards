package org.cardanofoundation.explorer.rewards.config;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import org.cardanofoundation.explorer.rewards.constant.NetworkConstants;
import rest.koios.client.backend.api.account.AccountService;
import rest.koios.client.backend.api.network.NetworkService;
import rest.koios.client.backend.api.pool.PoolService;
import rest.koios.client.backend.factory.BackendFactory;
import rest.koios.client.backend.factory.BackendService;

@Component
@Profile("koios")
public class KoiosClient {

  @Value("${application.network}")
  private String value;

  @Value("${application.koios-base-url-enabled}")
  private Boolean baseUrlEnabled;

  @Value("${application.koios-base-url}")
  private String baseUrl;

  private BackendService backendService;

  public AccountService accountService() {
    return this.backendService.getAccountService();
  }

  public NetworkService networkService() {
    return this.backendService.getNetworkService();
  }

  public PoolService poolService() {
    return this.backendService.getPoolService();
  }

  @PostConstruct
  void setBackendService() {
    if(Boolean.TRUE.equals(baseUrlEnabled)) {
      this.backendService = BackendFactory.getCustomRPCService(baseUrl);
    } else{
      this.backendService = switch (value) {
        case NetworkConstants.MAINNET -> BackendFactory.getKoiosMainnetService();
        case NetworkConstants.PREPROD -> BackendFactory.getKoiosPreprodService();
        case NetworkConstants.PREVIEW -> BackendFactory.getKoiosPreviewService();
        case NetworkConstants.GUILDNET -> BackendFactory.getKoiosGuildService();
        default -> throw new IllegalStateException("Unexpected value: " + value);
      };
    }
  }
}
