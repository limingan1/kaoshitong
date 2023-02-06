[string]$global:Source_Path=Get-Location
$global:Target_Log = $global:Source_Path + "\Reinforcement.log"
function Write_Log($msg,$cout = 0)
{
	if($cout -eq 0)
	{
		Write-Host $msg
	}
	[string]$date = Get-Date
	$ThisLogString = $date + ">>"+$msg
	if((test-path $global:Target_Log) -eq $false)
	{
		new-item -path $global:Source_Path -name Reinforcement.log -type "file" -ErrorAction "SilentlyContinue"
	}

	Add-Content $global:Target_Log $ThisLogString
}
function Get_Config_Name_Cmd($serName)
{
   $rStr = CMD /C "sc qc $serName"
   $name = ""
   $rStr | ForEach {
        if ($_.contains("SERVICE_START_NAME :"))
        {
            $name = $_.Replace("SERVICE_START_NAME :", "").trim().Replace(".\", "")
        }
   }
   
   return $name
}

function openSqlServer(){
	Set-Location -Path "$global:Source_Path\tools\SCM"
	
	.\restoregpo.bat "$global:Source_Path"
	[string[]]$userNames = @()
	$dbUserName = Get_Config_Name_Cmd("`"HUAWEI SMC 2.0 DataBase`"")
	if ($dbUserName -ne "")
	{
		 $userNames += $dbUserName
	}
	$sqlExName = Get_Config_Name_Cmd("`"MSSQL`$SQLEXPRESS`"")
	if ($sqlExName -ne "")
	{
		 $userNames += $sqlExName
	}
	$sqlUserName = Get_Config_Name_Cmd("`"MSSQLSERVER`"")
	if ($sqlUserName -ne "")
	{
		 $userNames += $sqlUserName
	}
	$sqlAgentUserName = Get_Config_Name_Cmd("`"SQLSERVERAGENT`"")
	if ($sqlAgentUserName -ne "")
	{
		 $userNames += $sqlAgentUserName
	}
	$sqlAnalysisName = Get_Config_Name_Cmd("`"MSSQLServerOLAPService`"")
	if ($sqlAnalysisName -ne "")
	{
		 $userNames += $sqlAnalysisName
	}
	Set_User_LogOnAsAService $userNames
	Start-Sleep -s 5
	Restart-Computer -Force
}
	
	
function Set_User_LogOnAsAService([string[]]$userNames)
{
	
	[string[]]$userSids = @()
	[string]$sqlagentsid = ""
	foreach ($username in $userNames)
	{
		$sidstr = $null
		try
		{
			$ntprincipal = New-Object System.Security.Principal.NTAccount "$userName"
			$sid = $ntprincipal.Translate([System.Security.Principal.SecurityIdentifier])
			$sidstr = $sid.Value.ToString()
			if ($username -eq $sqlAgentUserName)
			{
				$sqlagentsid = $sidstr
			}
			$userSids += $sidstr
		}
		catch
		{
			$sidstr = $null
		}
	}
	if($userSids.Length -eq 0)
	{
		Write_Log "Account not found!" -ForegroundColor Red
		exit -1
	}
	$userSidsStr = [system.String]::Join(",*", $userSids)
	$tmp = ""
	$tmp = [System.IO.Path]::GetTempFileName()
	Write-Host "Export current local policy..."
	secedit.exe /export /cfg "$($tmp)" 
	$c = ""
	$c = Get-Content -Path $tmp
	$SeBatchLogonRight_currentSetting = ""
	$SeServiceLogonRight_currentSetting = ""
	$SeAssignPrimaryTokenPrivilege_currentSetting = ""
	$SeDenyNetworkLogonRight_currentSetting = ""
	$SeInteractiveLogonRight_currentSetting = ""
	foreach($s in $c) 
	{
		if( $s -like "SeBatchLogonRight*") 
		{
			$x = $s.split("=",[System.StringSplitOptions]::RemoveEmptyEntries)
			$SeBatchLogonRight_currentSetting = $x[1].Trim()
		}
		if( $s -like "SeServiceLogonRight*") 
		{
			$x = $s.split("=",[System.StringSplitOptions]::RemoveEmptyEntries)
			$SeServiceLogonRight_currentSetting = $x[1].Trim()
		}
		if( $s -like "SeAssignPrimaryTokenPrivilege*") 
		{
			$x = $s.split("=",[System.StringSplitOptions]::RemoveEmptyEntries)
			$SeAssignPrimaryTokenPrivilege_currentSetting = $x[1].Trim()
		}
		if( $s -like "SeDenyNetworkLogonRight*") 
		{
			$x = $s.split("=",[System.StringSplitOptions]::RemoveEmptyEntries)
			$SeDenyNetworkLogonRight_currentSetting = $x[1].Trim()
		}
		if( $s -like "SeInteractiveLogonRight*")
		{
			$x = $s.split("=",[System.StringSplitOptions]::RemoveEmptyEntries)
			$SeInteractiveLogonRight_currentSetting = $x[1].Trim()
		}
	}
	if( [string]::IsNullOrEmpty($SeBatchLogonRight_currentSetting) ) {
		$SeBatchLogonRight_currentSetting = "*$($userSidsStr)"
	} else {
		$SeBatchLogonRight_currentSetting = "*$($userSidsStr),$($SeBatchLogonRight_currentSetting)"
	}
	if( [string]::IsNullOrEmpty($SeServiceLogonRight_currentSetting) ) 
	{
		$SeServiceLogonRight_currentSetting = "*$($userSidsStr)"
	} else {
		$SeServiceLogonRight_currentSetting = "*$($userSidsStr),$($SeServiceLogonRight_currentSetting)"
	}
	if( [string]::IsNullOrEmpty($SeInteractiveLogonRight_currentSetting) ) 
	{
		$SeInteractiveLogonRight_currentSetting = "*$($userSidsStr)"
	} else {
		$SeInteractiveLogonRight_currentSetting = "*$($userSidsStr),$($SeInteractiveLogonRight_currentSetting)"
	}
	if (($global:FailoverInstall -eq $true) -or ($global:DualInstall -eq $true))
	{
		#Set SQLAGENT user 'Replace a process level token'
		if( [string]::IsNullOrEmpty($SeAssignPrimaryTokenPrivilege_currentSetting) ) 
		{
			$SeAssignPrimaryTokenPrivilege_currentSetting = "*$($sqlagentsid)"
		} else {
			$SeAssignPrimaryTokenPrivilege_currentSetting = "*$($sqlagentsid),$($SeAssignPrimaryTokenPrivilege_currentSetting)"
		}
		#Remove Local Account from 'Deny access to this computer from the network'
		if( $false -eq [string]::IsNullOrEmpty($SeDenyNetworkLogonRight_currentSetting) ) 
		{
			$SeDenyNetworkLogonRight_currentSetting = $SeDenyNetworkLogonRight_currentSetting.Replace("*S-1-5-114,", "").Replace(",*S-1-5-114", "").trim()
		}
	}
	$outfile = @"
[Unicode]
Unicode=yes
[Version]
signature="`$CHICAGO`$"
Revision=1
[Privilege Rights]
SeBatchLogonRight = $($SeBatchLogonRight_currentSetting)
SeServiceLogonRight = $($SeServiceLogonRight_currentSetting)
SeAssignPrimaryTokenPrivilege = $($SeAssignPrimaryTokenPrivilege_currentSetting)
SeDenyNetworkLogonRight = $($SeDenyNetworkLogonRight_currentSetting)
"@	
	$tmp2 = ""
	$tmp2 = [System.IO.Path]::GetTempFileName()
	Write-Host "Import new settings to Local Security Policy..."
	$outfile | Set-Content -Path $tmp2 -Encoding Unicode -Force
	Push-Location (Split-Path $tmp2)
	try
	{
		secedit.exe /configure /db "secedit.sdb" /cfg "$($tmp2)" /areas USER_RIGHTS 
	}
	finally
	{	
		Pop-Location
	}
}
function Act_Reboot
{
	Start-Sleep -s 5
	if ($global:ByPreinstall -eq $true)
	{
		Set-TopLevelLog(0)
	}
	else
	{
		Write_Log "The machine will be restarted sooner.."
		Start-Sleep -s 5		
	}
	Restart-Computer -Force
}

openSqlServer