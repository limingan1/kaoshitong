setlocal enabledelayedexpansion

taskkill /FI  "WINDOWTITLE eq cascadegw_checkdb.bat" /IM cmd.exe /F

%1 %2
ver|find "5.">nul&&goto :st
mshta vbscript:createobject("shell.application").shellexecute("%~s0","goto :st","","runas",1)(window.close)&goto :eof

:st
cd /d "%~dp0"
set PATH =%CD%

net stop cascadegw

cd..
SET C_PATH=%CD:\=\\%
wmic Process Where "Name='java.exe' and CommandLine LIKE '%%cascadegwr4_startup.jar%%'" Call Terminate


SET GW_HOME=%CD%
move %GW_HOME%\tools\VDC.txt %GW_HOME%\VDC.txt

SET serverName=cascadegw

net remove %serverName% confirm

SET EPATH=%GW_HOME%\cascadegwr3.0.jar
SET JAVA_DIR=%GW_HOME%\jdk\bin

::执行数据库初始化脚本SQLCMD -E -dmaster -iinitDB.sql > ./logs/checkdb.out
osql -E -i %CD%\tools\initDB.sql >> ./logs/checkdb.out
@echo init db over


cd tools

del %GW_HOME%\dual\dual_init.ps1
del %GW_HOME%\tools\install1.bat
del %GW_HOME%\tools\checkdb.bat
del %GW_HOME%\tools\checkdb1.bat

SET JAVA_OPTS= -server
SET JAVA_OPTS=%JAVA_OPTS% -Dlog4j2.formatMsgNoLookups=true -jar

SET JAVA_OPTS=%JAVA_OPTS% -Xmx2048m
SET JAVA_OPTS=%JAVA_OPTS% -Xms2048m
SET JAVA_OPTS=%JAVA_OPTS% -Xmn1024m
SET JAVA_OPTS=%JAVA_OPTS% -Xss512k
SET JAVA_OPTS=%JAVA_OPTS% -XX:MetaspaceSize=512m
SET JAVA_OPTS=%JAVA_OPTS% -XX:MaxMetaspaceSize=512m

nssm.exe install %serverName% %JAVA_DIR%\java.exe %JAVA_OPTS% -jar %EPATH%
nssm.exe set %serverName% AppDirectory "%GW_HOME%"
nssm.exe set %serverName% Description "VDC Cascadegw"
nssm.exe set %serverName% Start SERVICE_AUTO_START
nssm.exe set %serverName% AppStdoutCreationDisposition 4
nssm.exe set %serverName% AppStderrCreationDisposition 4
nssm.exe set %serverName% AppRotateFiles  1
nssm.exe set %serverName% AppRotateBytes  1048576


@echo off
echo.
echo.
netsh advfirewall firewall show rule name="Enable port 8090 - TCP" >nul
if not ERRORLEVEL 1 (
    echo "Enable port 8090 - TCP" exit
    netsh advfirewall firewall delete rule name="Enable port 8090 - TCP" protocol = TCP localport = 8090
)

netsh advfirewall firewall show rule name="Enable port 5443 - TCP" >nul
if not ERRORLEVEL 1 (
    echo "Enable port 5443 - TCP" exit
) else (
    echo "Enable port 5443 - TCP" starting...
    netsh advfirewall firewall add rule name = "Enable port 5443 - TCP" dir = in action = allow protocol = TCP localport = 5443
)


echo.
echo.
echo ****************************************
echo cascadegw is starting...
net start %serverName%
echo ****************************************
pause

ENDLOCAL
del %0