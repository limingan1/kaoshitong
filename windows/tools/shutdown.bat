@echo off
setlocal enabledelayedexpansion

%1 %2
ver|find "5.">nul&&goto :st
mshta vbscript:createobject("shell.application").shellexecute("%~s0","goto :st","","runas",1)(window.close)&goto :eof

:st
net stop cascadegw

wmic Process Where "Name='java.exe' and CommandLine LIKE '%%cascadegwr3.0.jar%%'" Call Terminate
ENDLOCAL