package org.cardanofoundation.explorer.rewards.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import org.cardanofoundation.explorer.rewards.constant.NetworkConstants;
import rest.koios.client.backend.api.account.AccountService;
import rest.koios.client.backend.api.network.NetworkService;
import rest.koios.client.backend.factory.BackendFactory;
import rest.koios.client.backend.factory.BackendService;

@Component
public class KoiosClient {
  @Value("${koios.network}")
  String value;

  public AccountService accountService(){
    return getBackendService().getAccountService();
  }

  public NetworkService networkService(){
    return getBackendService().getNetworkService();
  }

  private BackendService getBackendService() {
    return switch (value) {
      case NetworkConstants.PREPROD -> BackendFactory.getKoiosPreprodService();
      case NetworkConstants.PREVIEW -> BackendFactory.getKoiosPreviewService();
      case NetworkConstants.GUILDNET -> BackendFactory.getKoiosGuildService();
      default -> BackendFactory.getKoiosMainnetService();
    };
  }
}
