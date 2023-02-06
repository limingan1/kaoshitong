--Start Snapshot Agent
use [vdmcas3]
EXEC sp_startpublication_snapshot @publication = 'cascadegw_Publication';
GO