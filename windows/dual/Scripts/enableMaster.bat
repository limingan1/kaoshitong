@echo off & color 0b & cls
Title Dual VDC System
%windir%\system32\WindowsPowerShell\v1.0\powershell.exe -file .\Dual_Enable_Master.ps1
@exit /b %result%
