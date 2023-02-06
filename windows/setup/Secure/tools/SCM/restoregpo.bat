@echo off
:: restoregpo, restore the gpo which is backuped by importgpo
if "%~1" == "" ( echo "restoregpo <vdc installation folder>"
exit /b )

set vdcfolder=%~1

:: check backup folder(save the backup in current folder)
set gpobackup=%vdcfolder%\gpobackup
if not exist "%gpobackup%" (
echo "Can't find the path <%gpobackup%>."
exit /b
)


:: import and backup registry.pol
"%vdcfolder%\setup\Secure\Tools\SCM\RegistryPolTool.exe" /file "%gpobackup%\registry.backup.pol" /logfile "%gpobackup%\registrypoltool.log"

auditpol /restore /file:"%gpobackup%\audit.backup.csv"
if exist "%gpobackup%\backup.db" (
del "%gpobackup%\backup.db"
)
secedit /import /db "%gpobackup%\backup.db" /cfg "%gpobackup%\GptTmpl.backup.inf"
secedit /configure /db "%gpobackup%\backup.db" /overwrite

gpupdate /force