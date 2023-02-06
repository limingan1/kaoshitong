cd /d %~dp0
cd ../config/
findstr /m "\[systemCfg\]" application-top.properties >nul
if %errorlevel% equ 0 (
    goto end
) else (
del application-top.properties.bak
    ren application-top.properties application-top.properties.bak
    echo [systemCfg] >> application-top.properties
    type config.ini.bak >> application-top.properties
)
:end
