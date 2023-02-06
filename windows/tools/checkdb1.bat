cd /d "%~dp0"
nssm stop cascadegw
cd..
SET C_PATH=%CD:\=\\%
goto nextSet

::执行数据库初始化脚本SQLCMD -E -dmaster -iinitDB.sql > ./logs/checkdb.out
osql -E -i %CD%\tools\initDB.sql >> ./logs/checkdb.out
@echo init db over

:nextSet
wmic Process Where "Name='java.exe' and CommandLine LIKE '%%cascadegwr3.0.jar%%'" Call Terminate

set GW_HOME=%CD%
set GW_FLAG=check

SET EPATH=%GW_HOME%\cascadegwr3.0.jar

SET JAVA_DIR=%GW_HOME%/jdk/bin

%JAVA_DIR%\java.exe -cp %GW_HOME%\tools\encry_decry_tool.jar Install cas %GW_HOME%
del %GW_HOME%\tools\encry_decry_tool.jar
del %GW_HOME%\tools\openssl.exe

SET JAVA_OPTS= -server
SET JAVA_OPTS=%JAVA_OPTS% -DGW_HOME=%GW_HOME%
SET JAVA_OPTS=%JAVA_OPTS% -DLOG_LEVEL=DEBUG
SET JAVA_OPTS=%JAVA_OPTS% -DGW_FLAG=check
SET JAVA_OPTS=%JAVA_OPTS% -Dfile.encoding=UTF-8
@echo wait ...
@echo %JAVA_DIR%
@echo %JAVA_OPTS%
@echo %EPATH%
%JAVA_DIR%\java.exe %JAVA_OPTS% -jar %EPATH%
ENDLOCAL
