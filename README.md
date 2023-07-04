# Cardano Explorer Rewards

### Reports
[Mutation report](https://cardano-foundation.github.io/cf-explorer-rewards/mutation-report/)

## Environment value
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