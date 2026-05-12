@echo off
setlocal EnableDelayedExpansion

if not exist "target\embedded.classpath.txt" (
    echo Missing target\embedded.classpath.txt
    echo Run a Maven build once before using this launcher.
    exit /b 1
)

set /p DEPENDENCY_CP=<target\embedded.classpath.txt
set "DEPENDENCY_CP=!DEPENDENCY_CP:C:\Users\sonuk=%USERPROFILE%!"
set "APP_CP=target\classes;%DEPENDENCY_CP%"

if not defined EMS_PORT set "EMS_PORT=8081"

java -cp "%APP_CP%" com.ems.bootstrap.AppLauncher
