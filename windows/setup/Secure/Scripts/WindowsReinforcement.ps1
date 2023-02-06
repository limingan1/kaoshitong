[string]$global:Source_Path=Get-Location
$global:OSTYPE = ""
$global:Target_Log = ""
[bool]$global:ByPreinstall=$false
$global:sqlAgentUserName=""
#Record VMD log function
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

function Set-TopLevelLog([int]$exitCode)
{
	if($exitCode -eq 0)
	{
		Write_Log "The machine should be rebooted!"
	}
	
	Exit $exitCode
}

#Get OS version
function Get_OSType
{
	try
	{
		Restart-Service -Name "Winmgmt" -Force -WarningAction SilentlyContinue -ErrorAction SilentlyContinue
	}
	catch
	{
		Write_Log -msg "Restart service failed." -cout 1
	}
	Start-Sleep -s 10
	$colOperatingSystem = Get-WmiObject -Query "select * from Win32_OperatingSystem"
	if($colOperatingSystem.Count -eq $null)
	{
		$arrayOpVer = $colOperatingSystem.Version.split('.')
		$strOpVer = $arrayOpVer[0] + '.' + $arrayOpVer[1]
		$strBuildVer = $arrayOpVer[2]
		$strPrdType = $colOperatingSystem.ProductType
		switch($strOpVer)
		{
			"6.1"
			{
				if ($strPrdType -eq 3)
				{
					$global:OSTYPE = "WS08R2"
				} 
				else
				{
					Write_Log "[Error]Please check the system is used for server." -color "red"
				}
			}
			"6.3"
			{
				if ($strPrdType -eq 3)
				{
					$global:OSTYPE = "WS12R2"
				}
				else
				{
					Write_Log "[Error]Please check the system is used for server." -color "red"
				}
			}
			"10.0"
            {
                switch($strBuildVer)
                {
                    "14393"
                    {
                        if($strPrdType -eq 3)
                        {
                            $global:OSTYPE = "WS16"
                        }
                        else
                        {
                            Write_Log "[Error]Please check the system is used for server." -color "red"
                        }
                    }
                    "17763"
                    {
                        if($strPrdType -eq 3)
                        {
                            $global:OSTYPE = "WS19"
                        }
                        else
                        {
                            Write_Log "[Error]Please check the system is used for server." -color "red"
                        }
                    }
                }
            }
		}
	}
	if ([String]::IsNullOrEmpty($global:OSTYPE))
	{
		Write_Log "[Error]The system is not supported." -color "red"
		Invoke-Item $Target_Log
		Start-Sleep -s 30
		FailConfirm(1)
	}
	if(($global:OSTYPE -eq "WS08R2") -and ($colOperatingSystem.BuildNumber -ne 7601))
	{
		Write_Log "[Error]Operating system 2008 R2 SP1 is required. Please install the SP1 patch manually, and then install SMC2.0 again!" -color "red"
		FailConfirm(1)
	}
}

function Excute_SetGPO
{
	Set-Location -Path "$global:Source_Path\tools\SCM"
	$CIS_Path = $global:Source_Path + "\tools\SCM\2012 CIS member server"
	if($global:OSTYPE -eq "WS08R2")
	{
		$CIS_Path = $global:Source_Path + "\tools\SCM\CIS member server(GPO Backup)"
	}
	Write_Log "Confirm the system is " + $global:OSTYPE
	
	.\importgpo.bat "$CIS_Path" "$global:Source_Path"
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

function Get_Service_Cmd($serName, $cmd)
{
   $rStr = CMD /C "sc qc $serName"
   $path = ""
   $rStr | ForEach {
        if ($_.contains("BINARY_PATH_NAME   :"))
        {
            $path = $_.Replace("BINARY_PATH_NAME   :", "").trim()
        }
   }
   
   if ($path -ne "")
   {
      $cmd = $cmd +  ' program="' + $path + '"'
      return $cmd
   }
   else
   {
      return $cmd
   }
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


function Set_Install_Path
{
	$global:Target_Log = $global:Source_Path + "\Reinforcement.log"
}


$srvsViolateSec="Alerter",
				"ClipSrv",
				"Dfs",
				"MSDTC",
				"Fax",
				"PolicyAgent",
				"LicenseService",
				"dmadmin",
				"Messenger",
				"mnmsrvc",
				"NetDDE",
				"NetDDEdsdm",
				"Spooler",
				"RasMan",
				"RemoteRegistry",
				"NtmsSvc",
				"SCardSvr",
				"SCardDrv",
				"SCPolicySvc",
				"Schedule", 
				"SQLBrowser",
				"TapiSrv",
				"TlntSvr",
				"RasAuto",
				"Dnscache",#Port:UDP5355
				"IKEEXT", #Port:UDP500&4500
				"UmRdpService",#Port:3389
				"TermService",
				"msiserver", 
				"WinTarget",
				"seclogon"
				
function Set_SrvsDown
{
	$fc=$false
	$isDomainUser=$false
    Import-Module failoverclusters -ErrorAction SilentlyContinue
    if($? -eq $true)
    {
        $fc=$true
    }
	
	$item = gwmi win32_service -Filter name='"vdc_pgsql"' | format-list StartName | Out-String
	
	if($item)
	{
		$user = $item.Substring($item.IndexOf(":")+1).Trim()
	
		if($user -ne ".\vdc_user")
		{
			$isDomainUser=$true
		}
	}

    foreach ($srv in $srvsViolateSec)
    {
		if(($srv -eq "DFS") -or ($srv -eq "MSDTC") -or ($srv -eq "RemoteRegistry") -or ($srv -eq "LanmanServer"))
		{
			if($fc -eq $true) { continue }
		}
		#only for win2012r2 cluster
		if(($srv -eq "Dnscache") -and ($global:OSTYPE -eq "WS12R2"))
		{
			#only for domain user
			if($isDomainUser -eq $true)
			{
				$probe = Get-Service -Name "$srv" -ErrorAction SilentlyContinue
				if ($probe -ne $null)
				{
					$srvth = Get-WmiObject win32_service -filter " name='$($probe.name)' "
					if($srvth.StartMode -eq "Disabled"){
						Set-Service -Name $probe.Name -StartupType Automatic -ErrorAction SilentlyContinue
					}
					if($probe.Status -eq "Stopped") {
						Start-Service -Name $probe.Name -ErrorAction SilentlyContinue
					}
				}
				continue
			}
		}
		
		$probe = Get-Service -Name "$srv" -ErrorAction SilentlyContinue
    	if ($probe -ne $null)
        {
			$srvth = Get-WmiObject win32_service -filter " name='$($probe.name)' "
            if($probe.Status -eq "Running") {
                Stop-Service -Name $probe.Name -Force -ErrorAction SilentlyContinue}
            if($srvth.StartMode -ne "Disabled"){
                Set-Service -Name $probe.Name -StartupType Disabled -ErrorAction SilentlyContinue 
            }
        }
    }
}


function Windows_Reinforcement
{
	try
	{
		Set_Install_Path | Out-Null

		Write_Log "$('*'*60)`nBeginning secure operating system..."
		Get_OSType | Out-Null
		Excute_SetGPO | Out-Null
		Set_SrvsDown | Out-Null
		Act_Reboot | Out-Null
		if($? -eq $true)
		{
			if ($global:ByPreinstall -eq $true)
			{
				Set-TopLevelLog(0)
			}
		}		
	}
	catch
	{
		if($global:ByPreinstall -eq $true)
		{
			Set-TopLevelLog(1)
		}
	}	
}


Windows_Reinforcement