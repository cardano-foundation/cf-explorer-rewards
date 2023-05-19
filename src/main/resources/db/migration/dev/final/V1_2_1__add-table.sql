
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


CREATE SEQUENCE IF NOT EXISTS epoch_stake_checkpoint_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE CACHE 1;

ALTER SEQUENCE epoch_stake_checkpoint_id_seq OWNED BY epoch_stake_checkpoint.id;

CREATE INDEX IF NOT EXISTS epoch_stake_checkpoint_epoch_checkpoint_idx ON epoch_stake_checkpoint USING btree (epoch_checkpoint);


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


CREATE SEQUENCE IF NOT EXISTS reward_checkpoint_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE CACHE 1;

ALTER SEQUENCE reward_checkpoint_id_seq OWNED BY reward_checkpoint.id;

CREATE INDEX IF NOT EXISTS reward_checkpoint_epoch_checkpoint_idx ON reward_checkpoint USING btree (epoch_checkpoint);

