--
-- CzechIdM 9 Flyway script 
-- BCV solutions s.r.o.
--
-- Add attributes for identity-role

CREATE TABLE idm_concept_role_form_value (
    id bytea NOT NULL,
    created timestamp NOT NULL,
    creator varchar(255) NOT NULL,
    creator_id bytea NULL,
    modified timestamp NULL,
    modifier varchar(255) NULL,
    modifier_id bytea NULL,
    original_creator varchar(255) NULL,
    original_creator_id bytea NULL,
    original_modifier varchar(255) NULL,
    original_modifier_id bytea NULL,
    realm_id bytea NULL,
    transaction_id bytea NULL,
    boolean_value bool NULL,
    byte_value bytea NULL,
    confidential bool NOT NULL,
    date_value timestamp NULL,
    double_value numeric(38,4) NULL,
    long_value int8 NULL,
    persistent_type varchar(45) NOT NULL,
    seq int2 NULL,
    short_text_value varchar(2000) NULL,
    string_value text NULL,
    uuid_value bytea NULL,
    attribute_id bytea NOT NULL,
    owner_id bytea NOT NULL,
    CONSTRAINT idm_concept_role_form_value_pkey PRIMARY KEY (id),
    CONSTRAINT idm_concept_role_form_value_seq_check CHECK ((seq <= 99999))
);
CREATE INDEX idx_concept_rol_form_a ON idm_concept_role_form_value USING btree (owner_id);
CREATE INDEX idx_concept_rol_form_a_def ON idm_concept_role_form_value USING btree (attribute_id);
CREATE INDEX idx_concept_rol_form_stxt ON idm_concept_role_form_value USING btree (short_text_value);
CREATE INDEX idx_concept_rol_form_uuid ON idm_concept_role_form_value USING btree (uuid_value);

CREATE TABLE idm_concept_role_form_value_a (
    id bytea NOT NULL,
    rev int8 NOT NULL,
    revtype int2 NULL,
    created timestamp NULL,
    created_m bool NULL,
    creator varchar(255) NULL,
    creator_m bool NULL,
    creator_id bytea NULL,
    creator_id_m bool NULL,
    modifier varchar(255) NULL,
    modifier_m bool NULL,
    modifier_id bytea NULL,
    modifier_id_m bool NULL,
    original_creator varchar(255) NULL,
    original_creator_m bool NULL,
    original_creator_id bytea NULL,
    original_creator_id_m bool NULL,
    original_modifier varchar(255) NULL,
    original_modifier_m bool NULL,
    original_modifier_id bytea NULL,
    original_modifier_id_m bool NULL,
    realm_id bytea NULL,
    realm_id_m bool NULL,
    transaction_id bytea NULL,
    transaction_id_m bool NULL,
    boolean_value bool NULL,
    boolean_value_m bool NULL,
    byte_value bytea NULL,
    byte_value_m bool NULL,
    confidential bool NULL,
    confidential_m bool NULL,
    date_value timestamp NULL,
    date_value_m bool NULL,
    double_value numeric(38,4) NULL,
    double_value_m bool NULL,
    long_value int8 NULL,
    long_value_m bool NULL,
    persistent_type varchar(45) NULL,
    persistent_type_m bool NULL,
    seq int2 NULL,
    seq_m bool NULL,
    short_text_value varchar(2000) NULL,
    short_text_value_m bool NULL,
    string_value text NULL,
    string_value_m bool NULL,
    uuid_value bytea NULL,
    uuid_value_m bool NULL,
    attribute_id bytea NULL,
    form_attribute_m bool NULL,
    owner_id bytea NULL,
    owner_m bool NULL,
    CONSTRAINT idm_concept_role_form_value_a_pkey PRIMARY KEY (id, rev),
    CONSTRAINT fk_t5r1wexotm521kj3ix9jprj9h FOREIGN KEY (rev) REFERENCES idm_audit(id)
);