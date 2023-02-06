@echo off
:: importgpo replace microsoft's localgpo to import and backup local group policys.
if "%~1" == "" ( echo "importgpo <GPO backup folder> <VDC dispatching installation folder>"
exit /b )

set folder=%~1
if "%~2" == "" (
set "vdcfolder=."
) else (
set "vdcfolder=%~2"
)
set needbackup=false

:: check backup folder(save the backup in current folder)
set gpobackup=%vdcfolder%\gpobackup
if not exist "%gpobackup%" (
mkdir "%gpobackup%"
set needbackup=true
)

:: backup audit
if %needbackup% == true (
auditpol /backup /file:"%gpobackup%\audit.backup.csv"
)

:: backup secedit
if %needbackup% == true (
secedit /generaterollback /cfg "%folder%\DomainSysvol\GPO\Machine\microsoft\windows nt\SecEdit\GptTmpl.inf" /rbk "%gpobackup%\GptTmpl.backup.inf" /log "%gpobackup%\secedit.log" /quiet
)

:: import and backup registry.pol
if %needbackup% == true (
"%vdcfolder%\setup\Secure\Tools\SCM\RegistryPolTool.exe" /file "%folder%\DomainSysvol\GPO\Machine\registry.pol" /backupfile "%gpobackup%\registry.backup.pol" /logfile "%gpobackup%\registrypoltool.log"
) else (
"%vdcfolder%\setup\Secure\Tools\SCM\RegistryPolTool.exe" /file "%folder%\DomainSysvol\GPO\Machine\registry.pol" /logfile "%gpobackup%\registrypoltool.log"
)

auditpol /restore /file:"%folder%\DomainSysvol\GPO\Machine\microsoft\windows nt\Audit\audit.csv"
secedit /import /db "%folder%\DomainSysvol\GPO\Machine\microsoft\windows nt\SecEdit\GptTmpl.db" /cfg "%folder%\DomainSysvol\GPO\Machine\microsoft\windows nt\SecEdit\GptTmpl.inf"
secedit /configure /db "%folder%\DomainSysvol\GPO\Machine\microsoft\windows nt\SecEdit\GptTmpl.db" /overwrite

gpupdate /force >nul