function Set_User_LogOnAsAService([string[]]$userNames)
{
	
	[string[]]$userSids = @()
	[string]$sqlagentsid = ""
	foreach ($username in $userNames)
	{
		$sidstr = $null
		try {
			$ntprincipal = New-Object System.Security.Principal.NTAccount "$userName"
			$sid = $ntprincipal.Translate([System.Security.Principal.SecurityIdentifier])
			$sidstr = $sid.Value.ToString()
			if ($username -eq $sqlAgentUserName)
			{
				$sqlagentsid = $sidstr
			}
			$userSids += $sidstr
		} catch {
			$sidstr = $null
		}
	}
	
	if( $userSids.Length -eq 0 ) {
		Write_Log "Account not found!" -ForegroundColor Red
		exit -1
	}
	$userSidsStr = [system.String]::Join(",*", $userSids)
	$tmp = ""
	$tmp = [System.IO.Path]::GetTempFileName()
	Write-Host "Export current local security policy..."
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
	try {

		secedit.exe /configure /db "secedit.sdb" /cfg "$($tmp2)" /areas USER_RIGHTS 
	} finally {	
		Pop-Location
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

function Set_VDC_User_Logon
{
    [string[]]$userNames = @()
    $dbUserName = Get_Config_Name_Cmd("`"vdc_pgsql`"")
    if ($dbUserName -ne "")
    {
         $userNames += $dbUserName
    }
    Set_User_LogOnAsAService $userNames
}

Set_VDC_User_Logon