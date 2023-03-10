--Dropping the merge pull subscription: Script to be run at Subscriber

use [vdmcas3]
exec sp_dropmergepullsubscription @publisher = N'$(Slave_Server_Name)', @publisher_db = N'vdmcas3', @publication = N'cascadegw_Publication'
GO

use vdmcas3
exec sp_removedbreplication
go

DECLARE @result varchar(1000)
DECLARE @num int
SET @result = ''
set @num=0
SELECT @num =count(*) FROM msdb.dbo.sysjobs where Name LIKE '%-vdmcas3-cascadegw_Publication-%'

WHILE @num > 0
BEGIN
	SELECT top 1 @result = job_id FROM msdb.dbo.sysjobs where Name LIKE '%-vdmcas3-cascadegw_Publication-%'
	USE msdb
	EXEC sp_delete_job  
		@job_id = @result; 

    SET @num = @num - 1
END
