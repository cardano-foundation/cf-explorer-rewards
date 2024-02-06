package org.cardanofoundation.explorer.rewards.config;

import jakarta.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import rest.koios.client.backend.api.account.AccountService;
import rest.koios.client.backend.api.epoch.EpochService;
import rest.koios.client.backend.api.network.NetworkService;
import rest.koios.client.backend.api.pool.PoolService;
import rest.koios.client.backend.factory.BackendFactory;
import rest.koios.client.backend.factory.BackendService;

import org.cardanofoundation.explorer.rewards.constant.NetworkConstants;

@Component
@Profile("koios")
public class KoiosClient {

  @Value("${application.network}")
  private String value;

  @Value("${application.koios-base-url-enabled}")
  private Boolean baseUrlEnabled;

  @Value("${application.koios-base-url}")
  private String baseUrl;

  @Value("${application.koios-auth-token}")
  private String authToken;

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

  public EpochService epochService() {
    return this.backendService.getEpochService();
  }

  @PostConstruct
  void setBackendService() {
    if (Boolean.TRUE.equals(baseUrlEnabled)) {
      backendService = BackendFactory.getCustomRPCService(baseUrl, authToken);
    } else {
      backendService =
          switch (value) {
            case NetworkConstants.MAINNET -> BackendFactory.getKoiosMainnetService(authToken);
            case NetworkConstants.PREPROD -> BackendFactory.getKoiosPreprodService(authToken);
            case NetworkConstants.PREVIEW -> BackendFactory.getKoiosPreviewService(authToken);
            case NetworkConstants.GUILDNET -> BackendFactory.getKoiosGuildService(authToken);
            default -> throw new IllegalStateException("Unexpected value: " + value);
          };
    }
  }
}
