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
  CONSTRAINT document_document_unique_uuid UNIQUE (uuid),
  CONSTRAINT document_document_check_state CHECK ( state IN ('VALID', 'INVALID') )
);

-- Add a unique index with a partial index condition
CREATE UNIQUE INDEX document_document_unique_identity_type_state
  ON document (identity_id, type, state)
  WHERE state = 'VALID';
