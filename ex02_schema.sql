CREATE SCHEMA estatesmgmt AUTHORIZATION root;

-- estatesmgmt.contract definition

-- Drop table

-- DROP TABLE estatesmgmt.contract;

CREATE TABLE estatesmgmt.contract (
	id serial NOT NULL,
	"date" date NOT NULL,
	place varchar NOT NULL,
	CONSTRAINT contract_pk PRIMARY KEY (id)
);


-- estatesmgmt.estate definition

-- Drop table

-- DROP TABLE estatesmgmt.estate;

CREATE TABLE estatesmgmt.estate (
	id serial NOT NULL,
	city varchar NOT NULL,
	postal_code int4 NOT NULL,
	street varchar NOT NULL,
	street_number int4 NOT NULL,
	square_area int4 NOT NULL,
	CONSTRAINT estate_pk PRIMARY KEY (id)
);


-- estatesmgmt.estate_agent definition

-- Drop table

-- DROP TABLE estatesmgmt.estate_agent;

CREATE TABLE estatesmgmt.estate_agent (
	"Name" varchar NOT NULL,
	address date NOT NULL,
	login varchar NOT NULL,
	"password" varchar NOT NULL,
	id serial NOT NULL,
	CONSTRAINT estate_agent_pk PRIMARY KEY (id),
	CONSTRAINT estate_agent_un UNIQUE (login)
);


-- estatesmgmt.person definition

-- Drop table

-- DROP TABLE estatesmgmt.person;

CREATE TABLE estatesmgmt.person (
	id serial NOT NULL,
	first_name varchar NOT NULL,
	"name" varchar NOT NULL,
	address varchar NOT NULL,
	CONSTRAINT person_pk PRIMARY KEY (id)
);


-- estatesmgmt.apartment definition

-- Drop table

-- DROP TABLE estatesmgmt.apartment;

CREATE TABLE estatesmgmt.apartment (
	estate_id int4 NOT NULL,
	balcony bool NOT NULL DEFAULT false,
	built_in_kitchen bool NOT NULL DEFAULT false,
	rooms int4 NOT NULL,
	floors int4 NOT NULL,
	rent float4 NOT NULL,
	CONSTRAINT apartment_pk PRIMARY KEY (estate_id),
	CONSTRAINT apartment_fk FOREIGN KEY (estate_id) REFERENCES estatesmgmt.estate(id) ON DELETE CASCADE
);


-- estatesmgmt.house definition

-- Drop table

-- DROP TABLE estatesmgmt.house;

CREATE TABLE estatesmgmt.house (
	floors int4 NOT NULL,
	price float4 NOT NULL,
	garden bool NOT NULL DEFAULT false,
	estate_id int4 NOT NULL,
	CONSTRAINT house_pk PRIMARY KEY (estate_id),
	CONSTRAINT house_fk FOREIGN KEY (estate_id) REFERENCES estatesmgmt.estate(id) ON DELETE CASCADE
);


-- estatesmgmt.manages definition

-- Drop table

-- DROP TABLE estatesmgmt.manages;

CREATE TABLE estatesmgmt.manages (
	estate_id int4 NOT NULL,
	agent_id int4 NOT NULL,
	CONSTRAINT manages_pk PRIMARY KEY (estate_id, agent_id),
	CONSTRAINT manages_un UNIQUE (estate_id),
	CONSTRAINT manages_fk_agent FOREIGN KEY (agent_id) REFERENCES estatesmgmt.estate_agent(id),
	CONSTRAINT manages_fk_estate FOREIGN KEY (estate_id) REFERENCES estatesmgmt.estate(id)
);


-- estatesmgmt.purchase_contract definition

-- Drop table

-- DROP TABLE estatesmgmt.purchase_contract;

CREATE TABLE estatesmgmt.purchase_contract (
	installments_no int4 NOT NULL DEFAULT 1,
	interest_rate float4 NULL,
	contract_id int4 NOT NULL,
	CONSTRAINT purchase_contract_pk PRIMARY KEY (contract_id),
	CONSTRAINT purchase_contract_fk FOREIGN KEY (contract_id) REFERENCES estatesmgmt.contract(id) ON DELETE CASCADE
);


-- estatesmgmt.sells definition

-- Drop table

-- DROP TABLE estatesmgmt.sells;

CREATE TABLE estatesmgmt.sells (
	person_id int4 NOT NULL,
	house_id int4 NOT NULL,
	purchase_contract_id int4 NOT NULL,
	CONSTRAINT sells_house_1 UNIQUE (house_id),
	CONSTRAINT sells_pk PRIMARY KEY (purchase_contract_id, house_id, person_id),
	CONSTRAINT sells_purchase_contract_1 UNIQUE (purchase_contract_id),
	CONSTRAINT sells_fk FOREIGN KEY (house_id) REFERENCES estatesmgmt.house(estate_id),
	CONSTRAINT sells_fk_person FOREIGN KEY (person_id) REFERENCES estatesmgmt.person(id),
	CONSTRAINT sells_fk_purchase_contract FOREIGN KEY (purchase_contract_id) REFERENCES estatesmgmt.purchase_contract(contract_id)
);


-- estatesmgmt.tenancy_contract definition

-- Drop table

-- DROP TABLE estatesmgmt.tenancy_contract;

CREATE TABLE estatesmgmt.tenancy_contract (
	start_date date NOT NULL DEFAULT CURRENT_DATE,
	duration interval NOT NULL,
	additional_costs float4 NOT NULL DEFAULT 0,
	contract_id int4 NOT NULL,
	CONSTRAINT tenancy_contract_pk PRIMARY KEY (contract_id),
	CONSTRAINT tenancy_contract_fk FOREIGN KEY (contract_id) REFERENCES estatesmgmt.contract(id) ON DELETE CASCADE
);


-- estatesmgmt.rents definition

-- Drop table

-- DROP TABLE estatesmgmt.rents;

CREATE TABLE estatesmgmt.rents (
	apartment_id int4 NOT NULL,
	person_id int4 NOT NULL,
	tenancy_contract_id int4 NOT NULL,
	CONSTRAINT rents_apartment_1 UNIQUE (apartment_id),
	CONSTRAINT rents_contract_1 UNIQUE (tenancy_contract_id),
	CONSTRAINT rents_pk PRIMARY KEY (apartment_id, person_id, tenancy_contract_id),
	CONSTRAINT rents_fk_apartment FOREIGN KEY (apartment_id) REFERENCES estatesmgmt.apartment(estate_id),
	CONSTRAINT rents_fk_contract FOREIGN KEY (tenancy_contract_id) REFERENCES estatesmgmt.tenancy_contract(contract_id),
	CONSTRAINT rents_fk_person FOREIGN KEY (person_id) REFERENCES estatesmgmt.person(id)
);