:st
copy "%~0" "%windir%\system32\"
cd /d "%~dp0"
cd..
SET C_PATH=%CD:\=\\%

echo Y|cacls %CD%\dual /p cascadegw:F administrators:F system:F administrator:F
net share "replData=%CD%\dual\replData" /grant:cascadegw,full