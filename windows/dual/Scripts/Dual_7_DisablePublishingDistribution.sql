--Dropping the merge pull subscription: Script to be run at Publisher
use [vdmcas3]
exec sp_dropmergesubscription @subscription_type = N'pull', @publication = N'cascadegw_Publication', @subscriber = N'$(Slave_Server_Name)', @subscriber_db = N'vdmcas3'
GO

-- Dropping the merge publication
use [vdmcas3]
exec sp_dropmergepublication @publication = N'cascadegw_Publication'
GO

-- Disabling the replication database
use master
exec sp_replicationdboption @dbname = N'vdmcas3', @optname = N'merge publish', @value = N'false'
GO


declare @dbname sysname set @dbname='CascadegwDistribution'
IF EXISTS (SELECT * FROM SYSDATABASES WHERE name=@dbname) 
BEGIN 
        declare @s nvarchar(1000)
        declare tb cursor local for 
        select s='kill '+cast(spid as varchar) 
        from master..sysprocesses 
        where dbid=db_id(@dbname) open tb fetch next from tb into @s while @@fetch_status=0 
        begin exec(@s) fetch next from tb into @s end close tb deallocate tb 
END
GO

-- Dropping the distribution databases
use master
exec sp_dropdistributiondb @database = N'CascadegwDistribution'
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
	SELECT top 1 @result = job_id FROM msdb.dbo.sysjobs where  Name LIKE '%-vdmcas3-cascadegw_Publication-%'

	USE msdb
	EXEC sp_delete_job  
		@job_id = @result; 

    SET @num = @num - 1
END
