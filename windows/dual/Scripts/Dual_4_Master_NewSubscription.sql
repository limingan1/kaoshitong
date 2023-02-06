--Script to be run at Publisher
use [vdmcas3]
exec sp_addmergesubscription @publication = N'cascadegw_Publication', @subscriber = N'$(Slave_Server_Name)', @subscriber_db = N'vdmcas3', @subscription_type = N'pull', @subscriber_type = N'local', @subscription_priority = 0, @sync_type = N'Automatic'
GO