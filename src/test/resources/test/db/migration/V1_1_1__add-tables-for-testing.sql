--
-- Name: reward; Type: TABLE;
--
CREATE TABLE IF NOT EXISTS reward
(
    id              bigint                 NOT NULL,
    amount          numeric(20, 0)         NOT NULL,
    earned_epoch    bigint                 NOT NULL,
    spendable_epoch bigint                 NOT NULL,
    type            character varying(255) NOT NULL,
    addr_id         bigint                 NOT NULL,
    pool_id         bigint
    );

CREATE SEQUENCE reward_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE CACHE 1;

ALTER SEQUENCE reward_id_seq OWNED BY reward.id;

--
-- Name: reward id; Type: DEFAULT;
--

ALTER TABLE ONLY reward
    ALTER COLUMN id SET DEFAULT nextval('reward_id_seq'::regclass);

--
-- Name: reward reward_pkey; Type: CONSTRAINT;
--

ALTER TABLE ONLY reward
    ADD CONSTRAINT reward_pkey PRIMARY KEY (id);

--
-- Name: reward unique_reward; Type: CONSTRAINT;
--

ALTER TABLE ONLY reward
    ADD CONSTRAINT unique_reward UNIQUE (addr_id, type, earned_epoch, pool_id);
--
-- Name: reward_checkpoint; Type: TABLE;
--
CREATE TABLE IF NOT EXISTS reward_checkpoint
(
    id               bigserial    NOT NULL,
    view             varchar(255) NOT NULL,
    epoch_checkpoint int4         NOT NULL
    );

--
-- Name: reward_checkpoint unique_stake_address_checkpoint; Type: CONSTRAINT;
--
ALTER TABLE ONLY reward_checkpoint
    ADD CONSTRAINT unique_stake_address_checkpoint UNIQUE (view);

--
-- Name: reward_checkpoint reward_checkpoint_pkey; Type: CONSTRAINT;
--
ALTER TABLE ONLY reward_checkpoint ADD CONSTRAINT reward_checkpoint_pkey PRIMARY KEY (id);

--
-- Name: reward_checkpoint_id_seq; Type: SEQUENCE;
--
CREATE SEQUENCE IF NOT EXISTS reward_checkpoint_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE CACHE 1;

ALTER SEQUENCE reward_checkpoint_id_seq OWNED BY reward_checkpoint.id;

--
-- Name: epoch_stake; Type: TABLE;
--

CREATE TABLE IF NOT EXISTS epoch_stake
(
    id       bigint         NOT NULL,
    amount   numeric(20, 0) NOT NULL,
    epoch_no integer        NOT NULL,
    addr_id  bigint         NOT NULL,
    pool_id  bigint         NOT NULL
    );

--
-- Name: epoch_stake unique_stake; Type: CONSTRAINT;
--

ALTER TABLE ONLY epoch_stake
    ADD CONSTRAINT unique_stake UNIQUE (epoch_no, addr_id, pool_id);

--
-- Name: epoch_stake_id_seq; Type: SEQUENCE;
--

CREATE SEQUENCE epoch_stake_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE CACHE 1;

ALTER SEQUENCE epoch_stake_id_seq OWNED BY epoch_stake.id;

--
-- Name: epoch_stake id; Type: DEFAULT;
--

ALTER TABLE ONLY epoch_stake
ALTER COLUMN id SET DEFAULT nextval('epoch_stake_id_seq'::regclass);

--
-- Name: epoch_stake_checkpoint; Type: TABLE;
--
CREATE TABLE IF NOT EXISTS epoch_stake_checkpoint
(
    id               bigserial    NOT NULL,
    view             varchar(255) NOT NULL,
    epoch_checkpoint int4         NOT NULL
);

--
-- Name: epoch_stake epoch_stake_pkey; Type: CONSTRAINT;
--

ALTER TABLE ONLY epoch_stake
    ADD CONSTRAINT epoch_stake_pkey PRIMARY KEY (id);


--
-- Name: epoch_stake_checkpoint; Type: TABLE;
--
CREATE TABLE IF NOT EXISTS epoch_stake_checkpoint
(
    id               bigserial    NOT NULL,
    view             varchar(255) NOT NULL,
    epoch_checkpoint int4         NOT NULL
    );

--
-- Name: epoch_stake_checkpoint unique_stake_address_checkpoint_v2; Type: CONSTRAINT;
--
ALTER TABLE ONLY epoch_stake_checkpoint
    ADD CONSTRAINT unique_stake_address_checkpoint_v2 UNIQUE (view);

--
-- Name: epoch_stake_checkpoint epoch_stake_checkpoint_pkey; Type: CONSTRAINT;
--
ALTER TABLE ONLY epoch_stake_checkpoint ADD CONSTRAINT epoch_stake_checkpoint_pkey PRIMARY KEY (id);

--
-- Name: epoch_stake_checkpoint_id_seq; Type: SEQUENCE;
--
CREATE SEQUENCE IF NOT EXISTS epoch_stake_checkpoint_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE CACHE 1;

ALTER SEQUENCE epoch_stake_checkpoint_id_seq OWNED BY epoch_stake_checkpoint.id;

ALTER TABLE ONLY epoch_stake_checkpoint
ALTER COLUMN id SET DEFAULT nextval('epoch_stake_checkpoint_id_seq'::regclass);

--
-- Name: epoch_stake_checkpoint; Type: TABLE;
--
CREATE TABLE IF NOT EXISTS pool_history
(
    id               bigserial    NOT NULL,
    pool_id          varchar(255) NOT NULL,
    epoch_no         int4         NULL,
    active_stake     varchar(255) NULL,
    active_stake_pct float8       NULL,
    saturation_pct   float8       NULL,
    block_cnt        int4         NULL,
    delegator_cnt    int4         NULL,
    margin           numeric      NULL,
    fixed_cost       varchar(255) NULL,
    pool_fees        varchar(255) NULL,
    deleg_rewards    varchar(255) NULL,
    epoch_ros        numeric      NULL
);

--
-- Name: pool_history pool_history_pkey; Type: CONSTRAINT;
--
ALTER TABLE ONLY pool_history ADD CONSTRAINT pool_history_pkey PRIMARY KEY (id);

--
-- Name: pool_history unique_pool_history; Type: CONSTRAINT;
--
ALTER TABLE ONLY pool_history ADD CONSTRAINT unique_pool_history UNIQUE (pool_id, epoch_no);

--
-- Name: pool_history_id_seq; Type: SEQUENCE;
--
CREATE SEQUENCE IF NOT EXISTS pool_history_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE CACHE 1;

ALTER SEQUENCE pool_history_id_seq OWNED BY pool_history.id;



--
-- Name: pool_history_checkpoint; Type: TABLE;
--
CREATE TABLE IF NOT EXISTS pool_history_checkpoint
(
     id               bigserial     NOT NULL,
     "view"           varchar(255)  NOT NULL,
     epoch_checkpoint int4          NOT NULL
);

--
-- Name: pool_history_checkpoint pool_history_checkpoint_pkey; Type: CONSTRAINT;
--
ALTER TABLE ONLY pool_history_checkpoint ADD CONSTRAINT pool_history_checkpoint_pkey PRIMARY KEY (id);

--
-- Name: pool_history_checkpoint unique_pool_history_checkpoint; Type: CONSTRAINT;
--
ALTER TABLE ONLY pool_history_checkpoint ADD CONSTRAINT unique_pool_history_checkpoint UNIQUE (view);

--
-- Name: pool_history_checkpoint_id_seq; Type: SEQUENCE;
--
CREATE SEQUENCE IF NOT EXISTS pool_history_checkpoint_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE CACHE 1;

ALTER SEQUENCE pool_history_checkpoint_id_seq OWNED BY pool_history_checkpoint.id;



--
-- Name: pool_info; Type: TABLE;
--
CREATE TABLE IF NOT EXISTS pool_info
(
    id               bigserial      NOT NULL,
    pool_id          varchar(255)   NOT NULL,
    fetched_at_epoch int4           NOT NULL,
    active_stake     varchar(255)   NULL,
    live_stake       varchar(255)   NULL,
    live_saturation  float8         NULL
);

--
-- Name: pool_info pool_info_pkey; Type: CONSTRAINT;
--
ALTER TABLE ONLY pool_info ADD CONSTRAINT pool_info_pkey PRIMARY KEY (id);

--
-- Name: pool_info unique_pool_info; Type: CONSTRAINT;
--
ALTER TABLE ONLY pool_info ADD CONSTRAINT unique_pool_info UNIQUE (pool_id, fetched_at_epoch);

--
-- Name: pool_info_id_seq; Type: SEQUENCE;
--
CREATE SEQUENCE IF NOT EXISTS pool_info_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE CACHE 1;

ALTER SEQUENCE pool_info_id_seq OWNED BY pool_info.id;



--
-- Name: pool_info_checkpoint; Type: TABLE;
--
CREATE TABLE IF NOT EXISTS pool_info_checkpoint
(
    id bigserial NOT NULL,
    "view" varchar(255) NULL,
    epoch_checkpoint int4 NULL
);


--
-- Name: pool_info_checkpoint pool_info_checkpoint_pkey; Type: CONSTRAINT;
--
ALTER TABLE ONLY pool_info_checkpoint ADD CONSTRAINT pool_info_checkpoint_pkey PRIMARY KEY (id);

--
-- Name: pool_info_checkpoint pool_info_checkpoint_view_key; Type: CONSTRAINT;
--
ALTER TABLE ONLY pool_info_checkpoint ADD CONSTRAINT pool_info_checkpoint_view_key UNIQUE (view);

--
-- Name: pool_info_checkpoint_id_seq; Type: SEQUENCE;
--
CREATE SEQUENCE IF NOT EXISTS pool_info_checkpoint_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE CACHE 1;

ALTER SEQUENCE pool_info_checkpoint_id_seq OWNED BY pool_info_checkpoint.id;

ALTER TABLE pool_history_checkpoint ADD earned_reward bool NULL DEFAULT false;
