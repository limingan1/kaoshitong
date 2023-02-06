cd /d %~dp0
cd ..
set C_PATH=%CD:\=\\%
cd tools
set USER_PATH=%CD:\=\\%
cd ../config
set filePATH=%CD%

echo off
setlocal enabledelayedexpansion
set "file=%filePATH%\application-top.properties"
set "file_personal=%filePATH%\application-top.properties.bak"
(
    for /f "tokens=*" %%i in (%file%) do (
        set s=%%i
        set s=!s:app_path=%C_PATH%!
        set s=!s:user_path=%USER_PATH%!
        echo !s!
    )
)>application-top.properties.bak
move /Y application-top.properties.bak application-top.properties