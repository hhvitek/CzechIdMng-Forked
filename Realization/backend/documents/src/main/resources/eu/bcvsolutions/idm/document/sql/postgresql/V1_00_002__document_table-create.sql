--
-- CzechIdM 7.0 Flyway script 
-- BCV solutions s.r.o.
--
-- This SQL script creates the required tables by CzechIdM (module example)

CREATE TABLE document
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

  uuid bytea NOT NULL,
  type TEXT NOT NULL,
  number integer NOT NULL,
  first_name character varying(255) NOT NULL,
  last_name character varying(255) NOT NULL,
  state TEXT NOT NULL,

  identity_id bytea NOT NULL,

  CONSTRAINT document_document_pkey PRIMARY KEY (id),
  CONSTRAINT document_document_ux_uuid UNIQUE (uuid),
  CONSTRAINT document_document_fk_idm_identity FOREIGN KEY (identity_id) REFERENCES idm_identity(id)
);

-- TODO Ensure that each identity can have only one VALID Document of each type
--

