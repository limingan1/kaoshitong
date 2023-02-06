if not EXISTS (select * From sysdatabases where name = 'vdmcas3')  CREATE DATABASE vdmcas3 COLLATE Chinese_PRC_CI_AS;

USE vdmcas3;

if not EXISTS(SELECT * FROM sys.Tables WHERE name='v3_vdm_smc_node') CREATE TABLE v3_vdm_smc_node (
  "id" varchar(32)  NOT NULL PRIMARY KEY,
  "name" varchar(128) NOT NULL,
  "area_code" varchar(64) ,
  "child_area_code" text ,
  "type" int DEFAULT 0,
  "ip" varchar(256) ,
  "ssl" int DEFAULT 0,
  "username" varchar(256) ,
  "password" varchar(256) ,
  "create_time" datetime,
  "update_time" datetime ,
  "en_type" varchar(128)  NOT NULL,
  "security_version" varchar(128) NOT NULL
)
go

if not EXISTS(SELECT * FROM sys.Tables WHERE name='v3_vdm_meeting_template') CREATE TABLE v3_vdm_meeting_template (
    "id" varchar(32)  NOT NULL PRIMARY KEY,
    "template_id" varchar(128) NOT NULL,
    "child" text NULL,
    "cascade_num" int null,
    "type" int NULL,
    "create_time" datetime
    )
;
go
if not EXISTS(SELECT * FROM sys.Tables WHERE name='v3_vdm_confernce_info') CREATE TABLE v3_vdm_confernce_info (
    "id" varchar(32)  NOT NULL PRIMARY KEY,
    "conference_id" varchar(128)  NOT NULL,
    "type" varchar(128),
    "participant_id" varchar(128)
    )
;
go
if not EXISTS(SELECT * FROM sys.Tables WHERE name='towall_lic') CREATE TABLE towall_lic (
    "id" int  NOT NULL PRIMARY KEY,
    "lic" text  NOT NULL,
    "isnew" bit ,
    "isbad" bit
    )
;
go
if not EXISTS(SELECT * FROM sys.Tables WHERE name='v3_vdm_vm_node') CREATE TABLE v3_vdm_vm_node (
  "id" varchar(32)  NOT NULL PRIMARY KEY,
  "parent_id" varchar(32),
  "name" varchar(128) NOT NULL,
  "area_code" varchar(64) ,
  "username" varchar(256) ,
  "password" varchar(256) ,
  "org_id" varchar(128) ,
  "permission_switch" int NULL,
  "create_time" datetime,
  "update_time" datetime ,
  "en_type" varchar(128)  NOT NULL,
  "security_version" varchar(128) NOT NULL
);
go
if not EXISTS(SELECT * FROM sys.Tables WHERE name='v3_vdm_org_user') CREATE TABLE v3_vdm_org_user (
  "id" varchar(32)  NOT NULL PRIMARY KEY,
  "node_id" varchar(32),
  "name" varchar(128) NOT NULL,
  "org_id" varchar(128) ,
  "username" varchar(256) ,
  "password" varchar(256) ,
  "en_type" varchar(128)  NOT NULL,
  "security_version" varchar(128) NOT NULL
);
go
ALTER TABLE v3_vdm_smc_node ADD  business_type int null;
go
ALTER TABLE v3_vdm_meeting_template ADD vmr_number varchar(64);
go
ALTER TABLE v3_vdm_smc_node ADD  smc_version varchar(64) null;
go
ALTER TABLE v3_vdm_smc_node ADD  vmr_conf_id varchar(128) null;
go
ALTER TABLE v3_vdm_smc_node ADD  permission_switch int null;
go
ALTER TABLE v3_vdm_smc_node ADD  client_id varchar(128) null;
go
ALTER TABLE v3_vdm_smc_node ADD  client_secret varchar(128) null;
go
ALTER TABLE v3_vdm_smc_node ADD  address_book_url varchar(128) null;
go
