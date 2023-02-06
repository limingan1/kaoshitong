$global:cascadegw_User_Name = "cascadegw"

function Delete_User
{
	$user = gwmi -Query "select * from win32_useraccount where name = '$global:cascadegw_User_Name'" -ErrorAction SilentlyContinue
	if ($user)
	{
		net user $global:cascadegw_User_Name /del
	}
	#Del vdc_user into Performance Monitor Users
   <#
	SID: S-1-5-32-558
	Name: BUILTIN\Performance Monitor Users
	
	SID: S-1-5-32-545
    Name: Users
	
	SID: S-1-5-32-556
	Name: BUILTIN\Network Configuration Operators
   #>
    $private:groupname=(gwmi win32_group -filter "LocalAccount=true and SID='S-1-5-32-545'").Name
    Try_Del_Group("$private:groupname")
    $private:groupname=(gwmi win32_group -filter "LocalAccount=true and SID='S-1-5-32-558'").Name
    Try_Del_Group("$private:groupname")
    $private:groupname=(gwmi win32_group -filter "LocalAccount=true and SID='S-1-5-32-556'").Name
    Try_Del_Group("$private:groupname")
}
function Try_Del_Group($groupName)
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
		net localgroup $groupName $global:cascadegw_User_Caption /del
	}
}

Delete_User