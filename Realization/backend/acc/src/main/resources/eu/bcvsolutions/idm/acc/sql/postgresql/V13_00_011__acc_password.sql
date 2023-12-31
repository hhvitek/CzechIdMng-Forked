--
-- CzechIdM 13.0 Flyway script
-- BCV solutions s.r.o.
--

----- TABLE acc_password -----
CREATE TABLE acc_password
(
    id bytea NOT NULL,
    created timestamp without time zone NOT NULL,
    creator character varying(255) NOT NULL,
    creator_id bytea,
    modified timestamp without time zone,
    modifier character varying(255),
    modifier_id bytea,
    original_creator character varying(255),
    original_creator_id bytea,
    original_modifier character varying(255),
    original_modifier_id bytea,
    realm_id bytea,
    transaction_id bytea,
    password character varying(255),
    account_id bytea NOT NULL,
    CONSTRAINT acc_password_pkey PRIMARY KEY (id),
    CONSTRAINT ux_acc_password_account UNIQUE (account_id)
);

----- TABLE acc_password_a -----
CREATE TABLE acc_password_a
(
    id bytea NOT NULL,
    rev bigint NOT NULL,
    revtype smallint,
    created timestamp without time zone,
    created_m boolean,
    creator character varying(255),
    creator_m boolean,
    creator_id bytea,
    creator_id_m boolean,
    modified timestamp without time zone,
    modified_m boolean,
    modifier character varying(255),
    modifier_m boolean,
    modifier_id bytea,
    modifier_id_m boolean,
    original_creator character varying(255),
    original_creator_m boolean,
    original_creator_id bytea,
    original_creator_id_m boolean,
    original_modifier character varying(255),
    original_modifier_m boolean,
    original_modifier_id bytea,
    original_modifier_id_m boolean,
    realm_id bytea,
    realm_id_m boolean,
    transaction_id bytea,
    transaction_id_m boolean,
    account_id bytea,
    account_m boolean,
    CONSTRAINT acc_password_a_pkey PRIMARY KEY (id, rev),
    CONSTRAINT fk_acc_password_rev FOREIGN KEY (rev)
        REFERENCES public.idm_audit (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION
);

----- TABLE acc_password_history -----
CREATE TABLE acc_password_history
(
    id bytea NOT NULL,
    created timestamp without time zone NOT NULL,
    creator character varying(255) NOT NULL,
    creator_id bytea,
    modified timestamp without time zone,
    modifier character varying(255),
    modifier_id bytea,
    original_creator character varying(255),
    original_creator_id bytea,
    original_modifier character varying(255),
    original_modifier_id bytea,
    realm_id bytea,
    transaction_id bytea,
    password character varying(255) NOT NULL,
    account_id bytea NOT NULL,
    CONSTRAINT acc_password_history_pkey PRIMARY KEY (id)
);

CREATE INDEX idx_acc_password_history_account
    ON acc_password_history USING btree
    (account_id ASC NULLS LAST);
