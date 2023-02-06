[string]$global:Source_Path=Get-Location
$global:cascadegw_User_Name = "cascadegw"
$global:cascadegw_User_Password = "Password@123"
$Target_Log= $global:Source_Path + "\install.log"
$global:VDC_User_Caption = ""

function Write_Log([string]$msg,[string]$color,[int]$cout = 0) 
{
	if($global:processing -eq $true)
	{
		$global:processbar = $global:processbar+1
	}
	if($cout -eq 0)
	{
		if($global:processbar -gt 100)
		{
			$global:processbar = 100
		}
		
		$temp = "[$global:processbar%] "+$msg
		if($color -eq "yellow")
		{
			Write-Host $temp -Foregroundcolor yellow -ErrorAction SilentlyContinue
		}
		elseif($color -eq "red")
		{
			Write-Host $temp -Foregroundcolor red -ErrorAction SilentlyContinue
		}
		else
		{
			Write-Host $temp
		}
	}
	[string]$date = Get-Date
	$ThisLogString = $date + ">>" + $msg
	if((test-path $Target_Log) -eq $false)
	{
		new-item -path $global:Source_Path -name install.log -type "file" -ErrorAction SilentlyContinue
	}
	
	Add-Content $Target_Log $ThisLogString -ErrorAction SilentlyContinue
}
#FailConfirm to exit
function FailConfirm([int]$exitcode = 0)
{
	if ($global:PreInstall)
	{
		if($exitcode -eq 0)
		{
			exit
		}
		else
		{
			exit $exitcode
		}
	}
	else
	{
	    Write-Host "Failed to install VDC,log file path is '$Target_Log'." -Foregroundcolor red -ErrorAction SilentlyContinue
		Read-host "Please confirm this error msg,then close the cmd window.(Default:Y)"
		exit
	}
}
function Create_User
{
    $user = gwmi -Query "select * from win32_useraccount where name = '$global:cascadegw_User_Name'" -ErrorAction SilentlyContinue
    if (!$user)
    {
        $domain = ""
        Write_Log -msg "Create account for VDC services !"
		net user $global:cascadegw_User_Name $global:cascadegw_User_Password /add $domain /passwordchg:no /expires:never /active:yes
		$user = gwmi -Query "select * from win32_useraccount where name = '$global:cascadegw_User_Name'"
		if ($user)
		{ 
			$global:VDC_User_Caption = $user.Caption
			if ($domain -eq "")
			{
				$user.PasswordExpires = $false
				$user.put()
			}
		}
		else 
		{
			Write_Log -msg "Failed to create account for VDC services!"
			Invoke-Item $Target_Log
            Start-Sleep -s 30
            FailConfirm(1)
		}

		#Add vdc_user into Performance Monitor Users
		<#
			SID: S-1-5-32-558
			Name: BUILTIN\Performance Monitor Users
			
			SID: S-1-5-32-545
			Name: Users
			
			SID: S-1-5-32-556
			Name: BUILTIN\Network Configuration Operators
		#>
		$private:groupname=(gwmi win32_group -filter "LocalAccount=true and SID='S-1-5-32-545'").Name
		Try_Add_Group("$private:groupname")
		$private:groupname=(gwmi win32_group -filter "LocalAccount=true and SID='S-1-5-32-558'").Name
		Try_Add_Group("$private:groupname")
		$private:groupname=(gwmi win32_group -filter "LocalAccount=true and SID='S-1-5-32-556'").Name
		Try_Add_Group("$private:groupname")
		
		#Set the account vdc_user for service loggin account
		#Set_User_LogOnAsAService
	}
	else
	{
		if ($user.getType().ToString() -eq "System.Object[]")
		{
			$global:cascadegw_User_Caption = $user[0].Caption
		}
		else
		{   
			$global:cascadegw_User_Caption = $user.Caption
		}
	}
    
    Set_Vdc_User_Premision
}
function Try_Add_Group($groupName)
{
	$isExist = $false
	net localgroup $groupName | foreach {
		if ($_.ToUpper().Contains($global:cascadegw_User_Name.ToUpper()))
		{
			$isExist = $true
		}
	}
	if ($isExist -eq $false)
	{
		net localgroup $groupName $global:VDC_User_Caption /add
	}
}
function Set_Vdc_User_Premision
{
    Write_Log -msg "Reinforce specialized account of vdc..."
    #Set the file access right
	cd $env:systemdrive
	$system32Path = $env:systemdrive + "\Windows\System32"
	cd $system32Path
	./icacls $global:Source_Path /remove "$global:cascadegw_User_Name"
    ###################################################
	##Set the particular permission on directory
	$M_VDCService = $global:cascadegw_User_Name + ":(OI)(CI)(F)"
	$RI_VDCService = $global:cascadegw_User_Name + ":(OI)(CI)R"
	$FO_VDCService = $global:cascadegw_User_Name + ":F"
	$RWDI_VDCService = $global:cascadegw_User_Name + ":(OI)(CI)(R,W,D)"
	$RWI_VDCService = $global:cascadegw_User_Name + ":(R,W)"
	##Set the particular permission on single file
	$M_VDCService_SingleFile = $global:cascadegw_User_Name + ":F"	
    
    Set_File_Permission -path "$global:Source_Path\.." -Permission $M_VDCService

    # SMC already reinforcement, need to grant premission for SWMaster user
    $user = gwmi -Query "select * from win32_useraccount where name = 'SWMaster'" -ErrorAction SilentlyContinue
    if ($user)
    {
        Set_File_Permission -path "$global:Source_Path\.." -Permission "SWMaster:(OI)(CI)(F)"
    }
}
#Grant the right
function Set_File_Permission([string]$Path,[string]$Permission)
{
	#Set the file access right
	cd $env:systemdrive
	$system32Path = $env:systemdrive + "\Windows\System32"
	cd $system32Path
	if (Test-Path $Path)
	{
		.\icacls $Path /grant $Permission
	}
	else
	{
		Write_Log -msg "When set the right of file, can't find it. path is $Path" -cout 1
	}
}



function DatabaseRole()
{
	$global:sql_Path = "{0}\addDatabaseUser.sql" -f $global:Source_Path
	$Params = "cascadegw_Service_User="+$env:COMPUTERNAME+"\"+$global:cascadegw_User_Name
	Do_SQL_Update "master" $global:sql_Path -params $Params | Out-File -Encoding ASCII -FilePath $Target_Log -Append
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
	return SQLCMD.EXE -S $env:COMPUTERNAME -d $dbName -i $SqlStr -v $params -N -C -l 100
}



Create_User
DatabaseRole

