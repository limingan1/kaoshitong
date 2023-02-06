set /p isPackage=is package mixserver?(y/n)
echo %isPackage%

cd "E:\code\cti_vdm_cascade\cascadegwr3.0"

"C:\Program Files\Git\bin\git.exe" reset --hard
"C:\Program Files\Git\bin\git.exe" pull --rebase
"C:\Program Files\Git\bin\git.exe" log -1

if %isPackage%==y (
	cd "E:\code\cti_vdm_mixServer"
    "C:\Program Files\Git\bin\git.exe" reset --hard
    "C:\Program Files\Git\bin\git.exe" pull --rebase
    "C:\Program Files\Git\bin\git.exe" log -1
    del /f/s/q E:\code\cti_vdm_mixServer\vdm-license\pom.xml
    move  E:\code\cti_vdm_cascade\cascadegwr3.0\windows\tools\pom.xml E:\code\cti_vdm_mixServer\vdm-license\
    call mvn clean install dependency:copy-dependencies -DoutputDirectory=target/lib
    pause
)

cd "E:\code\cti_vdm_cascade\cascadegwr3.0"

pause
set filename=\\172.16.70.6\release\vdm_release\%date:~0,4%%date:~5,2%%date:~8,2%
mkdir %filename%
rd E:\code\cti_vdm_cascade\cascadegwr3.0\target /s /q
del /f/s/q E:\code\cti_vdm_cascade\cascadegwr3.0\windows\cascadegwr3.0.jar
del /f/s/q E:\code\cti_vdm_cascade\cascadegwr3.0\windows\vdc-link-*.*

del /f/s/q E:\code\cti_vdm_cascade\cascadegwr3.0\api\src\main\java\com\suntek\vdm\gw\api\ApiApplication.java
move E:\code\cti_vdm_cascade\cascadegwr3.0\windows\tools\ApiApplication.java E:\code\cti_vdm_cascade\cascadegwr3.0\api\src\main\java\com\suntek\vdm\gw\api\

echo "begin package"
call mvn clean package
if not exist "target\cascadegwr3.0.jar" (
    echo "package error"
    pause
    exit
)
move E:\code\cti_vdm_cascade\cascadegwr3.0\target\cascadegwr3.0.jar E:\code\cti_vdm_cascade\cascadegwr3.0\windows\
rd E:\code\cti_vdm_cascade\cascadegwr3.0\windows\jdk
move E:\code\jdk E:\code\cti_vdm_cascade\cascadegwr3.0\windows\
ping -n 5 127.0.0.1
cd "E:\code\cti_vdm_cascade\cascadegwr3.0\windows"
"C:\Program Files (x86)\Inno Setup 5\ISCC" cascadegwr_setup.iss
if not exist E:\code\jdk move E:\code\cti_vdm_cascade\cascadegwr3.0\windows\jdk E:\code\
ping -n 5 127.0.0.1
copy *.exe %filename% /y
pause