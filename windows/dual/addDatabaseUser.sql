use  master
go
EXEC sp_grantlogin '$(cascadegw_Service_User)'
go

EXEC master.dbo.sp_addsrvrolemember @loginame=N'$(cascadegw_Service_User)', @rolename=N'sysadmin'
go

