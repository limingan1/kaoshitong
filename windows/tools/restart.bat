nssm.exe stop cascadegw confirm
wmic Process Where "Name='java.exe' and CommandLine LIKE '%%cascadegwr3.0.jar%%'" Call Terminate
nssm.exe start cascadegw confirm
