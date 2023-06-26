# Changelog

## [0.1.2](https://github.com/cardano-foundation/cf-explorer-rewards/compare/v0.1.1...v0.1.2) (2023-06-26)


### Features

* [met-1036] add unit tests in service, controller layer ([f6af6fa](https://github.com/cardano-foundation/cf-explorer-rewards/commit/f6af6fa93fbbcfe26f570fae1a25979d8433be29))
* MET-1321 ada-pots data fetching implement ([35d9980](https://github.com/cardano-foundation/cf-explorer-rewards/commit/35d9980abf60cda2e2196c2f701b2843b3d802e7))
* replace-jdbc-with-jooq-in-repository-level ([a9218a9](https://github.com/cardano-foundation/cf-explorer-rewards/commit/a9218a9c9fbb7f95b1b41b1ff806ff4e03ef5747))


### Bug Fixes

* [met-1036] update data handler logic in pool history, pool info, reward and epoch stake ([99fbaa0](https://github.com/cardano-foundation/cf-explorer-rewards/commit/99fbaa0f303dd948934d9997f262e0216a2ac468))
* **gha:** fix condition for main branch workflow trigger ([27fbf30](https://github.com/cardano-foundation/cf-explorer-rewards/commit/27fbf30bde35e5a3c7514fc95607dc91ba671339))
* **gha:** fixed typo ([811d4b9](https://github.com/cardano-foundation/cf-explorer-rewards/commit/811d4b915f2e1562e1539fa2ad192ae99273c172))

## [0.1.1](https://github.com/cardano-foundation/cf-explorer-rewards/compare/v0.1.0...v0.1.1) (2023-06-12)


### Features

* [met-1112] update flyway to add earned_reward column in pool_history_checkpoint table ([35ae401](https://github.com/cardano-foundation/cf-explorer-rewards/commit/35ae40154591c27526b234097320ce96f068a019))
* add script migration for checkpoint table and enable flyway ([ac2519e](https://github.com/cardano-foundation/cf-explorer-rewards/commit/ac2519e44bdb35517ecc88b0a893d75a5b5c2c3a))
* add unit tests for pool info, pool history jdbc repository ([f1613ec](https://github.com/cardano-foundation/cf-explorer-rewards/commit/f1613ec1f56fbbdf5b342ed133db4b64d5f2c52f))
* add unit tests for reward, epoch stake jdbc repository ([50e2d35](https://github.com/cardano-foundation/cf-explorer-rewards/commit/50e2d35fd99a06061c2c11642b7cede41d1d2155))
* MET-1140 fetch pool history, pool info data api ([2a29b62](https://github.com/cardano-foundation/cf-explorer-rewards/commit/2a29b628ef8a8a4f1b85bd6bf789743a55782f8a))


### Bug Fixes

* Dockerfile name ([39694b1](https://github.com/cardano-foundation/cf-explorer-rewards/commit/39694b12bb8e1117b838adbcaafa63ed7e862d53))


### Performance Improvements

* met-1110 improve performance reward, epoch-stake fetching ([f5281e6](https://github.com/cardano-foundation/cf-explorer-rewards/commit/f5281e60412f3ad8b409500f3fc729deca6bf858))

## 0.1.0 (2023-05-30)


### Features

* 0.1.0 release ([79e96a9](https://github.com/cardano-foundation/cf-explorer-rewards/commit/79e96a97d42c05e4ffcb970f9d04e338ec79f73f))
* met-1092 initial source, dependency, env and dockerfile ([59d9161](https://github.com/cardano-foundation/cf-explorer-rewards/commit/59d916120ea50afd6e9e6ee40e9689b7e74c3c27))
* met-1111 reward data fetching ([ea1ca33](https://github.com/cardano-foundation/cf-explorer-rewards/commit/ea1ca33c636922c68b29c82e33e46b07a7e9edad))
* met-1112 epoch-stake data fetching ([7b7c287](https://github.com/cardano-foundation/cf-explorer-rewards/commit/7b7c28735e7baa10c005a3ce3e49db651da897ae))