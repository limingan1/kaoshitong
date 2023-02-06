$global:Path_SQLCMD = ""
$Reg_ODBC = "ODBCToolsPath"
$Reg_Path_ODBC = "HKLM:\SOFTWARE\Microsoft\Microsoft SQL Server\150\Tools\ClientSetup"
if(!(Test-Path $Reg_Path_ODBC))
{
	$Reg_Path_ODBC = "HKLM:\SOFTWARE\Microsoft\Microsoft SQL Server\130\Tools\ClientSetup"
	if(!(Test-Path $Reg_Path_ODBC))
	{
		$Reg_Path_ODBC = "HKLM:\SOFTWARE\Microsoft\Microsoft SQL Server\120\Tools\ClientSetup"
	}
}
if(Test-Path $Reg_Path_ODBC)
{
	$global:Path_SQLCMD = (Get-ItemProperty $Reg_Path_ODBC $Reg_ODBC).$Reg_ODBC
}

$global:SMC_Service_User = $args[0]
$global:Master_Server_Name = $args[1]
$global:Slave_Server_Name = $args[2]
$global:SMC20_Install_Path = $args[3]
$global:Service_User_Pwd = $args[4]

$Target_Log = "{0}\ReplicationLog.txt" -f $global:SMC20_Install_Path

$global:Step1_Path = "{0}\Scripts\Dual_1_ConfigureDistribution.sql" -f $global:SMC20_Install_Path
$global:Step2_Path = "{0}\Scripts\Dual_2_CreatePublication.sql" -f $global:SMC20_Install_Path
$global:Step3_Path = "{0}\Scripts\Dual_3_StartSnapshotAgent.sql" -f $global:SMC20_Install_Path
$global:Step4_Path = "{0}\Scripts\Dual_4_Master_NewSubscription.sql" -f $global:SMC20_Install_Path
$global:Script_Path = "{0}\Scripts" -f $global:SMC20_Install_Path
function Write_Log([string]$msg) 
{
	[string]$date = Get-Date
	$ThisLogString = $date + ">>"+$msg
	Add-Content $Target_Log $ThisLogString -ErrorAction SilentlyContinue
}

function Do_SQL_Update($dbName, $SqlStr,[int]$count=0,[string[]]$params="x=`"y`"")
{
	$fail_count=0
	$result = Do_SQL_UpdateInit $dbName $SqlStr $params
	
	for($fail_count=0;$fail_count -lt 4;$fail_count++ )
	{
		[string]$str = $result | Out-String
		if($str -eq $null)
		{
			break
		}
		
		if($str.Contains("Msg ") -and $str.Contains("Level "))
		{
			Start-Sleep -s 1
			$result = Do_SQL_UpdateInit $dbName $SqlStr $params
		}
		else
		{
			break
		}
	}
	
	if($fail_count -ge 4 -and $count -eq 0)
	{
		[string]$str = $result | Out-String
		Write_Log -msg $str
		Write_Log -msg "[ERROR]:Cannot execute script <$SqlStr> in database <$dbName>!"
	}
	return $result
}

function Do_SQL_UpdateInit($dbName, $SqlStr,[string[]]$params)
{
    cd $global:Path_SQLCMD
	return .\SQLCMD.EXE -S $global:Master_Server_Name -d $dbName -i $SqlStr -v $params -N -C -l 100
}

Write_Log -msg "Begin Enable Master..."
#Replace SMC2.0 Install Path in sql
(Get-Content -Path $global:Step1_Path) | ForEach-Object {$_ -replace "SMC20_Install_Path",$global:SMC20_Install_Path} |Set-Content -Path $global:Step1_Path
(Get-Content -Path $global:Step2_Path) | ForEach-Object {$_ -replace "SCRIPT_PATH",$global:Script_Path} |Set-Content -Path $global:Step2_Path

#Dual_1_ConfigureDistribution
$Step1Param1 = "Master_Server_Name={0}" -f $global:Master_Server_Name
Do_SQL_Update "master" $global:Step1_Path -params $Step1Param1 | Out-File -Encoding ASCII -FilePath $Target_Log -Append

#Dual_2_CreatePublication
$Step2Param1 = "Master_Server_Name={0}" -f $global:Master_Server_Name
$Step2Param2 = "SMC_Service_User={0}" -f $global:SMC_Service_User
$Step2Param3 = "Service_User_Pwd={0}" -f $global:Service_User_Pwd
$Step2Param4 = "Slave_Server_Name={0}" -f $global:Slave_Server_Name
$Step2Params = $Step2Param1,$Step2Param2,$Step2Param3,$Step2Param4
Do_SQL_Update "master" $global:Step2_Path -params $Step2Params | Out-File -Encoding ASCII -FilePath $Target_Log -Append

#Dual_3_StartSnapshotAgent
Do_SQL_Update "master" $global:Step3_Path | Out-File -Encoding ASCII -FilePath $Target_Log -Append

#Dual_4_Master_NewSubscription
$Step4Param1 = "Slave_Server_Name={0}" -f $global:Slave_Server_Name
Do_SQL_Update "master" $global:Step4_Path -params $Step4Param1 | Out-File -Encoding ASCII -FilePath $Target_Log -Append