@echo off
setlocal

set "SCRIPT_DIR=%~dp0"
for %%I in ("%SCRIPT_DIR%..") do set "PROJECT_DIR=%%~fI"

if not defined JAVA_HOME (
    for /d %%I in ("C:\Program Files\Java\jdk-*") do (
        if exist "%%~fI\bin\java.exe" (
            set "JAVA_HOME=%%~fI"
        )
    )
)

if not defined CATALINA_HOME (
    if exist "%USERPROFILE%\Downloads\apache-tomcat-10.1.13-windows-x64\apache-tomcat-10.1.13\bin\startup.bat" (
        set "CATALINA_HOME=%USERPROFILE%\Downloads\apache-tomcat-10.1.13-windows-x64\apache-tomcat-10.1.13"
    )
)

if not defined CATALINA_HOME (
    if exist "%USERPROFILE%\Downloads\apache-tomcat-10.1.13\bin\startup.bat" (
        set "CATALINA_HOME=%USERPROFILE%\Downloads\apache-tomcat-10.1.13"
    )
)

if not defined JAVA_HOME (
    echo JAVA_HOME is not set and no JDK was found in C:\Program Files\Java.
    exit /b 1
)

if not defined CATALINA_HOME (
    echo CATALINA_HOME is not set and Apache Tomcat 10.1.13 was not found in Downloads.
    echo Set CATALINA_HOME to your Tomcat folder and run this script again.
    exit /b 1
)

if not exist "%CATALINA_HOME%\bin\startup.bat" (
    echo startup.bat was not found in "%CATALINA_HOME%\bin".
    exit /b 1
)

set "CATALINA_BASE=%PROJECT_DIR%\.tomcat-base"
if not defined EMS_DB_URL set "EMS_DB_URL=jdbc:mysql://localhost:3306/event_management_system?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC"
if not defined EMS_DB_USERNAME set "EMS_DB_USERNAME=root"
if not defined EMS_DB_PASSWORD set "EMS_DB_PASSWORD=Yadavsonu@@19o14u"

echo Using JAVA_HOME=%JAVA_HOME%
echo Using CATALINA_HOME=%CATALINA_HOME%
echo Using CATALINA_BASE=%CATALINA_BASE%

call "%CATALINA_HOME%\bin\startup.bat"
