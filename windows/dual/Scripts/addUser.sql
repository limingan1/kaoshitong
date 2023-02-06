use  master
go
EXEC sp_grantlogin '$(SMC_Service_User)'
go

EXEC master.dbo.sp_addsrvrolemember @loginame=N'$(SMC_Service_User)', @rolename=N'sysadmin'
go