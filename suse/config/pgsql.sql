CREATE DATABASE vdmcas
    WITH
    OWNER = vdcsuper
    ENCODING = 'UTF8'
    TABLESPACE = pg_default
    CONNECTION LIMIT = 50;

GRANT ALL ON DATABASE vdmcas TO vdcsuper;

GRANT TEMPORARY, CONNECT ON DATABASE vdmcas TO PUBLIC;

\c vdmcas;
/*
 Navicat Premium Data Transfer

 Source Server         : PSQL
 Source Server Type    : PostgreSQL
 Source Server Version : 130002
 Source Host           : localhost:5432
 Source Catalog        : vdm020
 Source Schema         : public

 Target Server Type    : PostgreSQL
 Target Server Version : 130002
 File Encoding         : 65001

 Date: 27/08/2021 20:02:29
*/


-- ----------------------------
-- Table structure for vdm_smc_node
-- ----------------------------
CREATE TABLE IF NOT EXISTS "public"."v3_vdm_smc_node" (
  "id" varchar(32) COLLATE "pg_catalog"."default" NOT NULL PRIMARY KEY,
  "name" varchar(128) COLLATE "pg_catalog"."default" NOT NULL,
  "area_code" varchar(64) COLLATE "pg_catalog"."default",
  "child_area_code" text COLLATE "pg_catalog"."default",
  "type" int4 DEFAULT 0,
  "ip" varchar(256) COLLATE "pg_catalog"."default",
  "ssl" int2 DEFAULT 0,
  "username" varchar(256) COLLATE "pg_catalog"."default",
  "password" varchar(256) COLLATE "pg_catalog"."default",
  "create_time" timestamp(6) DEFAULT now(),
  "update_time" timestamp(6) ,
  "en_type" varchar(128) COLLATE "pg_catalog"."default" NOT NULL,
  "security_version" varchar(128) COLLATE "pg_catalog"."default" NOT NULL
)
;


-- ----------------------------
-- Table structure for v3_vdm_meeting_template
-- ----------------------------
CREATE TABLE IF NOT EXISTS "public"."v3_vdm_meeting_template" (
    "id" varchar(32) COLLATE "pg_catalog"."default" NOT NULL PRIMARY KEY,
    "template_id" varchar(128) COLLATE "pg_catalog"."default" NOT NULL,
    "child" text NULL,
    "cascade_num" int null,
    "type" int NULL,
    "create_time" timestamp(6) DEFAULT now()
    )
;


-- ----------------------------
-- Table structure for v3_vdm_meeting_template
-- ----------------------------
CREATE TABLE IF NOT EXISTS "public"."v3_vdm_confernce_info" (
    "id" varchar(32) COLLATE "pg_catalog"."default" NOT NULL PRIMARY KEY,
    "conference_id" varchar(128) COLLATE "pg_catalog"."default" NOT NULL,
    "type" varchar(128),
    "participant_id" varchar(128)
    )
;

CREATE TABLE IF NOT EXISTS "public"."v3_vdm_vm_node" (
  "id" varchar(32) COLLATE "pg_catalog"."default" NOT NULL PRIMARY KEY,
  "parent_id" varchar(64) COLLATE "pg_catalog"."default",
  "name" varchar(128) COLLATE "pg_catalog"."default" NOT NULL,
  "area_code" varchar(64) COLLATE "pg_catalog"."default",
  "username" varchar(256) COLLATE "pg_catalog"."default",
  "password" varchar(256) COLLATE "pg_catalog"."default",
  "org_id" varchar(128) COLLATE "pg_catalog"."default",
  "permission_switch" int NULL,
  "create_time" timestamp(6) DEFAULT now(),
  "update_time" timestamp(6) ,
  "en_type" varchar(128) COLLATE "pg_catalog"."default" NOT NULL,
  "security_version" varchar(128) COLLATE "pg_catalog"."default" NOT NULL
);

CREATE TABLE IF NOT EXISTS "public"."v3_vdm_org_user" (
  "id" varchar(32) COLLATE "pg_catalog"."default" NOT NULL PRIMARY KEY,
  "node_id" varchar(64) COLLATE "pg_catalog"."default",
  "name" varchar(128) COLLATE "pg_catalog"."default" NOT NULL,
  "org_id" varchar(128) COLLATE "pg_catalog"."default",
  "username" varchar(256) COLLATE "pg_catalog"."default",
  "password" varchar(256) COLLATE "pg_catalog"."default",
  "en_type" varchar(128) COLLATE "pg_catalog"."default" NOT NULL,
  "security_version" varchar(128) COLLATE "pg_catalog"."default" NOT NULL
);

start transaction;
ALTER TABLE v3_vdm_smc_node ADD COLUMN IF NOT EXISTS business_type int;
COMMIT;
start transaction;
update v3_vdm_smc_node set business_type=1 where business_type is null;
COMMIT;

start transaction;
ALTER TABLE v3_vdm_smc_node ADD COLUMN IF NOT EXISTS smc_version varchar(64);
COMMIT;
start transaction;
update v3_vdm_smc_node set smc_version='3.0' where smc_version is null;
COMMIT;
start transaction;
ALTER TABLE v3_vdm_meeting_template ADD COLUMN IF NOT EXISTS vmr_number varchar(64);
COMMIT;
start transaction;
ALTER TABLE v3_vdm_smc_node ADD COLUMN IF NOT EXISTS permission_switch int;
COMMIT;

start transaction;
ALTER TABLE v3_vdm_smc_node ADD COLUMN IF NOT EXISTS vmr_conf_id varchar(128);
ALTER TABLE v3_vdm_smc_node ADD COLUMN IF NOT EXISTS client_id varchar(128);
ALTER TABLE v3_vdm_smc_node ADD COLUMN IF NOT EXISTS client_secret varchar(128);
ALTER TABLE v3_vdm_smc_node ADD COLUMN IF NOT EXISTS address_book_url varchar(128);
COMMIT;


