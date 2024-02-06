# Explorer Rewards Service

<p align="left">
<img alt="Tests" src="https://github.com/cardano-foundation/cf-explorer-rewards/actions/workflows/tests.yaml/badge.svg?branch=main" />
<img alt="Coverage" src="https://cardano-foundation.github.io/cf-explorer-authentication/badges/jacoco.svg" />
<img alt="Release" src="https://github.com/cardano-foundation/cf-explorer-rewards/actions/workflows/release.yaml/badge.svg?branch=main" />
<img alt="Publish" src="https://github.com/cardano-foundation/cf-explorer-rewards/actions/workflows/publish.yaml/badge.svg?branch=main" />
<a href="https://app.fossa.com/reports/a5bfbe13-4315-4515-a943-3b7e9581e94f"><img alt="FOSSA Status" src="https://app.fossa.com/api/projects/custom%2B41588%2Fgit%40github.com%3Acardano-foundation%2Fcf-explorer-rewards.git.svg?type=small"/></a>
<a href="https://conventionalcommits.org"><img alt="conventionalcommits" src="https://img.shields.io/badge/Conventional%20Commits-1.0.0-%23FE5196?logo=conventionalcommits" /></a>
</p>

This repository provides an API for retrieving reward data for stake pools and delegators. It utilizes [Koios](https://www.koios.rest/) to fetch the data, aggregates and stores it, and serves it for the cardano explorer.
It currently uses Koios to fetch the required data. A separate project to calculate the rewards without relying on DB Sync can be found [here](https://github.com/cardano-foundation/cf-java-rewards-calculation).

👉 Check the [Explorer repository](https://github.com/cardano-foundation/cf-explorer) to understand how the microservices work together

## 🧪 Test Reports

To ensure the stability and reliability of this project, unit and mutation tests have been implemented. By clicking on the links below, you can access the detailed test reports and review the outcomes of the tests performed.

📊 [Coverage Report](https://cardano-foundation.github.io/cf-explorer-rewards/coverage-report/)

📊 [Mutation Report](https://cardano-foundation.github.io/cf-explorer-rewards/mutation-report/)

## 🌱 Environment Variables
- SERVER_PORT: port running
- POSTGRES_HOST: database host
- POSTGRES_PORT: database port
- POSTGRES_DB: database name
- POSTGRES_USER: database access username
- POSTGRES_PASSWORD:database user password
- POSTGRES_SCHEMA: database schema
- NETWORK: network type
- SPRING_PROFILES_ACTIVE: active profiles
- POOL_CORE_SIZE: the ThreadPoolExecutor's core pool size
- POOL_MAX_SIZE: the ThreadPoolExecutor's maximum pool size
- REWARD_LIST_SIZE_EACH_THREAD: batch size to fetch reward data each thread
- EPOCH_STAKE_LIST_SIZE_EACH_THREAD: batch size to fetch epoch stake data each thread
- POOL_INFO_LIST_SIZE_EACH_THREAD: batch size to fetch pool info data each thread
- POOL_INFO_DATA_JOB_ENABLED: whether enable fetching pool info data job
- FETCH_AND_SAVE_POOL_INFO_DATA_DELAY: the interval between each run of the job to fetch pool info data
- FLYWAY_ENABLE: whether to enable flyway
- FLYWAY_VALIDATE: whether to automatically call validate when performing a migration
- KOIOS_BASE_URL_ENABLED: Set `true` to enable to use your Koios instance base URL, otherwise set `false`
- KOIOS_BASE_URL: Koios instance base URL.
- KOIOS_AUTH_TOKEN: JWT Bearer Auth token generated via https://koios.rest Profile page
