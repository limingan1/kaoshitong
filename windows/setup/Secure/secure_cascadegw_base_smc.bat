@echo off & color 0b & cls
Title VDC User Logon
cd /d "%~dp0"
set result=0
%windir%\system32\WindowsPowerShell\v1.0\powershell.exe set-executionpolicy remotesigned
%windir%\system32\WindowsPowerShell\v1.0\powershell.exe -file .\Scripts\SetCascadegwUserLogon.ps1
if %errorlevel% NEQ 0 ( 
set result=1
echo Failed to set cascadegw user logon . >> set_logon.txt
) else (
echo Set cascadegw user logon successful. >> set_logon.txt
)
net start cascadegw

@exit /b %result%