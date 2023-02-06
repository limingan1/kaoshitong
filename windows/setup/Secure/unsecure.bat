@echo off & color 0b & cls
Title Secure VDC System
cd /d "%~dp0"
set result=0
%windir%\system32\WindowsPowerShell\v1.0\powershell.exe set-executionpolicy remotesigned
%windir%\system32\WindowsPowerShell\v1.0\powershell.exe -file .\Scripts\openSqlserver.ps1
if %errorlevel% NEQ 0 ( 
set result=1
echo Failed to secure os. >> preinstall_save.txt
) else (
echo OS security reinforcement completed successful. >> preinstall_save.txt
)

@exit /b %result%