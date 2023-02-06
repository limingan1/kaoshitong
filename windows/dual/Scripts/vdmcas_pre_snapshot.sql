use [vdmcas3]
go

update [vdmcas3].[dbo].[DualConfig]
set ConfigValue = '<ConfigBoolean><Value>false</Value></ConfigBoolean>'
where ConfigId = 'vdmcasSnapshotApplyCompleted'
go