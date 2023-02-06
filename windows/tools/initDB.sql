if not EXISTS (select * From sysdatabases where name='vdmcas3') create database vdmcas3 COLLATE Chinese_PRC_CI_AS;
GO
use vdmcas3;
GO
exec sp_addrolemember 'db_owner', 'NT AUTHORITY\SYSTEM';
GO
exec sp_helpuser 'db_owner'
GO

