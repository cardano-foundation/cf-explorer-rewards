# Cardano Explorer Rewards

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
- LIST_SIZE_EACH_THREAD: batch size to fetch data each thread
- FLYWAY_ENABLE: whether to enable flyway
- FLYWAY_VALIDATE: whether to automatically call validate when performing a migration
- REWARD_PARALLEL_SAVING_ENABLED: whether to save rewards data concurrently
- REWARD_SUB_LIST_SIZE: reward data subset size  in case of concurrent saving
- REWARD_PARALLEL_SAVING_THREAD_NUM: number of thread to save rewards data in case of concurrent saving
- EPOCH_STAKE_PARALLEL_SAVING_ENABLED: whether to save epoch stake data concurrently
- EPOCH_STAKE_SUB_LIST_SIZE: epoch stake subset size  in case of concurrent saving
- EPOCH_STAKE_PARALLEL_SAVING_THREAD_NUM: number of thread to save epoch stake data in case of concurrent saving