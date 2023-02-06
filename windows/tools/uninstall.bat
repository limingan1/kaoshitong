@echo off
setlocal enabledelayedexpansion
nssm.exe stop cascadegw confirm
nssm.exe remove cascadegw confirm
net stop cascadegw

%1 %2
ver|find "5.">nul&&goto :st
mshta vbscript:createobject("shell.application").shellexecute("%~s0","goto :st","","runas",1)(window.close)&goto :eof


:st
copy "%~0" "%windir%\system32\"
cd /d "%~dp0"
nssm.exe stop cascadegw confirm
nssm.exe remove cascadegw confirm

%windir%\system32\WindowsPowerShell\v1.0\powershell.exe set-executionpolicy remotesigned
%windir%\system32\WindowsPowerShell\v1.0\powershell.exe -file .\uninstall.ps1 "%~1"


cd..
SET C_PATH=%CD:\=\\%

wmic Process Where "Name='java.exe' and CommandLine LIKE '%%cascadegwr4_startup.jar%%'" Call Terminate

ENDLOCAL