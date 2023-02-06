--Installing the server as a Distributor.
use master
exec sp_adddistributor @distributor = N'$(Master_Server_Name)', @heartbeat_interval=10, @password = N''
GO
exec sp_adddistributiondb @database = N'CascadegwDistribution',  @log_file_size = 2, @min_distretention = 0, @max_distretention = 72, @history_retention = 48, @security_mode = 1
GO
exec sp_MSupdate_agenttype_default @profile_id = 15
GO

use [CascadegwDistribution]
if (not exists (select * from sysobjects where name = 'UIProperties' and type = 'U ')) 
	create table UIProperties(id int) 
if (exists (select * from ::fn_listextendedproperty('SnapshotFolder', 'user', 'dbo', 'table', 'UIProperties', null, null))) 
	EXEC sp_updateextendedproperty N'SnapshotFolder', N'\\$(Master_Server_Name)\replData', 'user', dbo, 'table', 'UIProperties'
else 
	EXEC sp_addextendedproperty N'SnapshotFolder', N'\\$(Master_Server_Name)\replData', 'user', dbo, 'table', 'UIProperties'
GO

exec sp_adddistpublisher @publisher = N'$(Master_Server_Name)', @distribution_db = N'CascadegwDistribution', @security_mode = 1, @working_directory = N'\\$(Master_Server_Name)\replData', @trusted = N'false', @thirdparty_flag = 0, @publisher_type = N'MSSQLSERVER'
GO
