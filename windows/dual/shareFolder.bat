
for /f "tokens=2 delims=:" %%a in ('net share ^| findstr /i /c:"Data\ReplData"') do set shareData=%%a
for /f "tokens=2 delims=: " %%b in ('net share ^| findstr /i /c:"Data\ReplData"') do set dir=%%b

echo %shareData%
echo %dir%

set share=%dir%:%shareData%
net share ReplData /del

net share "ReplData=%share%" /grant:cascadegw,full /grant:smc20_service_user,full
