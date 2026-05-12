@echo off
setlocal

set "SCRIPT_DIR=%~dp0"
for %%I in ("%SCRIPT_DIR%..") do set "PROJECT_DIR=%%~fI"

if not defined CATALINA_HOME (
    if exist "%USERPROFILE%\Downloads\apache-tomcat-10.1.13-windows-x64\apache-tomcat-10.1.13\bin\shutdown.bat" (
        set "CATALINA_HOME=%USERPROFILE%\Downloads\apache-tomcat-10.1.13-windows-x64\apache-tomcat-10.1.13"
    )
)

if not defined CATALINA_HOME (
    if exist "%USERPROFILE%\Downloads\apache-tomcat-10.1.13\bin\shutdown.bat" (
        set "CATALINA_HOME=%USERPROFILE%\Downloads\apache-tomcat-10.1.13"
    )
)

if not defined JAVA_HOME (
    for /d %%I in ("C:\Program Files\Java\jdk-*") do (
        if exist "%%~fI\bin\java.exe" (
            set "JAVA_HOME=%%~fI"
        )
    )
)

if not defined CATALINA_HOME (
    echo CATALINA_HOME is not set and Apache Tomcat 10.1.13 was not found in Downloads.
    exit /b 1
)

if not defined JAVA_HOME (
    echo JAVA_HOME is not set and no JDK was found in C:\Program Files\Java.
    exit /b 1
)

set "CATALINA_BASE=%PROJECT_DIR%\.tomcat-base"

call "%CATALINA_HOME%\bin\shutdown.bat"
