package org.cardanofoundation.explorer.rewards.config;

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

  public AccountService accountService() {
    return this.getBackendService().getAccountService();
  }

  public NetworkService networkService() {
    return this.getBackendService().getNetworkService();
  }

  public PoolService poolService() {
    return this.getBackendService().getPoolService();
  }

  public EpochService epochService() {
    return this.getBackendService().getEpochService();
  }

  private BackendService getBackendService() {
    if (Boolean.TRUE.equals(baseUrlEnabled)) {
      return BackendFactory.getCustomRPCService(baseUrl);
    } else {
      return switch (value) {
        case NetworkConstants.MAINNET -> BackendFactory.getKoiosMainnetService();
        case NetworkConstants.PREPROD -> BackendFactory.getKoiosPreprodService();
        case NetworkConstants.PREVIEW -> BackendFactory.getKoiosPreviewService();
        case NetworkConstants.GUILDNET -> BackendFactory.getKoiosGuildService();
        default -> throw new IllegalStateException("Unexpected value: " + value);
      };
    }
  }
}
