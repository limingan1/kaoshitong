
--vdmcas
use [vdmcas3]
exec sp_replicationdboption @dbname = N'vdmcas3', @optname = N'merge publish', @value = N'true'
GO

use [vdmcas3]
exec sp_addmergepublication @publication = N'cascadegw_Publication', @description = N'Merge publication of database ''vdmcas3'' from Publisher ''$(Master_Server_Name)''.', @sync_mode = N'native', @retention = 100, @allow_push = N'true', @allow_pull = N'true', @allow_anonymous = N'true', @enabled_for_internet = N'false', @snapshot_in_defaultfolder = N'true', @compress_snapshot = N'false', @ftp_port = 21, @allow_subscription_copy = N'false', @add_to_active_directory = N'false', @dynamic_filters = N'false', @conflict_retention = 14, @keep_partition_changes = N'false', @allow_synctoalternate = N'false', @max_concurrent_merge = 0, @max_concurrent_dynamic_snapshots = 0, @use_partition_groups = null, @publication_compatibility_level = N'100RTM', @replicate_ddl = 1, @allow_subscriber_initiated_snapshot = N'false', @allow_web_synchronization = N'false', @allow_partition_realignment = N'true', @retention_period_unit = N'years', @conflict_logging = N'both', @automatic_reinitialization_policy = 0,  @generation_leveling_threshold = 0
GO

exec sp_addpublication_snapshot @publication = N'cascadegw_Publication', @frequency_type = 1, @frequency_interval = 14, @frequency_relative_interval = 1, @frequency_recurrence_factor = 0, @frequency_subday = 1, @frequency_subday_interval = 5, @active_start_time_of_day = 500, @active_end_time_of_day = 235959, @active_start_date = 0, @active_end_date = 0, @job_login = N'$(SMC_Service_User)', @job_password = N'$(Service_User_Pwd)', @publisher_security_mode = 1


use [vdmcas3]
exec sp_addmergearticle @publication = N'cascadegw_Publication', @article = N'v3_vdm_smc_node', @source_owner = N'dbo', @source_object = N'v3_vdm_smc_node', @type = N'table', @description = null, @creation_script = null, @pre_creation_cmd = N'drop', @schema_option = 0x000000010C034FD1, @identityrangemanagementoption = N'manual', @destination_owner = N'dbo', @force_reinit_subscription = 1, @column_tracking = N'false', @subset_filterclause = null, @vertical_partition = N'false', @verify_resolver_signature = 1, @allow_interactive_resolver = N'false', @fast_multicol_updateproc = N'true', @check_permissions = 0, @subscriber_upload_options = 1, @delete_tracking = N'true', @compensate_for_errors = N'false', @stream_blob_columns = N'false', @partition_options = 0
GO

use [vdmcas3]
exec sp_addmergearticle @publication = N'cascadegw_Publication', @article = N'v3_vdm_meeting_template', @source_owner = N'dbo', @source_object = N'v3_vdm_meeting_template', @type = N'table', @description = null, @creation_script = null, @pre_creation_cmd = N'drop', @schema_option = 0x000000010C034FD1, @identityrangemanagementoption = N'manual', @destination_owner = N'dbo', @force_reinit_subscription = 1, @column_tracking = N'false', @subset_filterclause = null, @vertical_partition = N'false', @verify_resolver_signature = 1, @allow_interactive_resolver = N'false', @fast_multicol_updateproc = N'true', @check_permissions = 0, @subscriber_upload_options = 1, @delete_tracking = N'true', @compensate_for_errors = N'false', @stream_blob_columns = N'false', @partition_options = 0
GO

use [vdmcas3]
exec sp_addmergearticle @publication = N'cascadegw_Publication', @article = N'v3_vdm_confernce_info', @source_owner = N'dbo', @source_object = N'v3_vdm_confernce_info', @type = N'table', @description = null, @creation_script = null, @pre_creation_cmd = N'drop', @schema_option = 0x000000010C034FD1, @identityrangemanagementoption = N'manual', @destination_owner = N'dbo', @force_reinit_subscription = 1, @column_tracking = N'false', @subset_filterclause = null, @vertical_partition = N'false', @verify_resolver_signature = 1, @allow_interactive_resolver = N'false', @fast_multicol_updateproc = N'true', @check_permissions = 0, @subscriber_upload_options = 1, @delete_tracking = N'true', @compensate_for_errors = N'false', @stream_blob_columns = N'false', @partition_options = 0
GO

use [vdmcas3]
exec sp_addmergearticle @publication = N'cascadegw_Publication', @article = N'v3_vdm_vm_node', @source_owner = N'dbo', @source_object = N'v3_vdm_vm_node', @type = N'table', @description = null, @creation_script = null, @pre_creation_cmd = N'drop', @schema_option = 0x000000010C034FD1, @identityrangemanagementoption = N'manual', @destination_owner = N'dbo', @force_reinit_subscription = 1, @column_tracking = N'false', @subset_filterclause = null, @vertical_partition = N'false', @verify_resolver_signature = 1, @allow_interactive_resolver = N'false', @fast_multicol_updateproc = N'true', @check_permissions = 0, @subscriber_upload_options = 1, @delete_tracking = N'true', @compensate_for_errors = N'false', @stream_blob_columns = N'false', @partition_options = 0
GO

use [vdmcas3]
exec sp_addmergearticle @publication = N'cascadegw_Publication', @article = N'v3_vdm_org_user', @source_owner = N'dbo', @source_object = N'v3_vdm_org_user', @type = N'table', @description = null, @creation_script = null, @pre_creation_cmd = N'drop', @schema_option = 0x000000010C034FD1, @identityrangemanagementoption = N'manual', @destination_owner = N'dbo', @force_reinit_subscription = 1, @column_tracking = N'false', @subset_filterclause = null, @vertical_partition = N'false', @verify_resolver_signature = 1, @allow_interactive_resolver = N'false', @fast_multicol_updateproc = N'true', @check_permissions = 0, @subscriber_upload_options = 1, @delete_tracking = N'true', @compensate_for_errors = N'false', @stream_blob_columns = N'false', @partition_options = 0
GO

--Add smc20_service_user Role to CascadegwDistribution
use [CascadegwDistribution]
exec sp_adduser '$(SMC_Service_User)'
go
EXEC sp_addrolemember 'db_owner','$(SMC_Service_User)'
go
