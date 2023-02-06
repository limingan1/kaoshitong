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

$global:Master_Server_Name = $args[0]
$global:Slave_Server_Name = $args[1]
$global:SMC20_Install_Path = $args[2]

$Target_Log = "{0}\ReplicationLog.txt" -f $global:SMC20_Install_Path
$global:Step6_Path = "{0}\Scripts\Dual_6_Delete_Subscription.sql" -f $global:SMC20_Install_Path

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

Write_Log -msg "Begin Disable Slave..."
#Dual_6_Delete_Subscription
$Step6Param1 = "Slave_Server_Name={0}" -f $global:Slave_Server_Name
Do_SQL_Update "master" $global:Step6_Path -params $Step6Param1 | Out-File -Encoding ASCII -FilePath $Target_Log -Append