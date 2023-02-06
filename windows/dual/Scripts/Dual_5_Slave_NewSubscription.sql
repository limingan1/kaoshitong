--Script to be run at Subscriber
DECLARE @num1 int
set @num1=0
SELECT @num1 =count(*) FROM msdb.dbo.sysjobs where Name LIKE '%-smc20db-smc20db_Publication-%' and category_id = 14
IF (@num1 = 1)
BEGIN
DECLARE @num int
set @num=0
SELECT @num =count(*) FROM msdb.dbo.sysjobs where Name LIKE '%-vdmcas3-cascadegw_Publication-%'

IF (@num = 0)
BEGIN
  use [vdmcas3]
  exec sp_addmergepullsubscription @publisher = N'$(Master_Server_Name)', @publication = N'cascadegw_Publication', @publisher_db = N'vdmcas3', @subscriber_type = N'Local', @subscription_priority = 0, @description = N'', @sync_type = N'Automatic'
  exec sp_addmergepullsubscription_agent @publisher = N'$(Master_Server_Name)', @publisher_db = N'vdmcas3', @publication = N'cascadegw_Publication', @distributor = N'$(Master_Server_Name)', @distributor_security_mode = 1, @distributor_login = N'', @distributor_password = null, @enabled_for_syncmgr = N'False', @frequency_type = 64, @frequency_interval = 0, @frequency_relative_interval = 0, @frequency_recurrence_factor = 0, @frequency_subday = 0, @frequency_subday_interval = 0, @active_start_time_of_day = 0, @active_end_time_of_day = 235959, @alt_snapshot_folder = N'', @working_directory = N'', @use_ftp = N'False', @job_login = N'$(SMC_Service_User)', @job_password = N'$(Service_User_Pwd)', @publisher_security_mode = 1, @publisher_login = null, @publisher_password = null, @use_interactive_resolver = N'False', @dynamic_snapshot_location = null, @use_web_sync = 0, @optional_command_line = N'-PollingInterval 30'
END
END
go