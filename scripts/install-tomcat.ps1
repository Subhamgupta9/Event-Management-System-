param(
    [string]$Version = "10.1.13",
    [string]$InstallRoot = "$env:USERPROFILE\Downloads"
)

$archiveName = "apache-tomcat-$Version-windows-x64.zip"
$downloadUrl = "https://archive.apache.org/dist/tomcat/tomcat-10/v$Version/bin/$archiveName"
$zipPath = Join-Path $InstallRoot $archiveName
$extractRoot = Join-Path $InstallRoot "apache-tomcat-$Version-windows-x64"
$tomcatHome = Join-Path $extractRoot "apache-tomcat-$Version"

if (Test-Path (Join-Path $tomcatHome "bin\startup.bat")) {
    Write-Host "Tomcat is already available at $tomcatHome"
    exit 0
}

New-Item -ItemType Directory -Force -Path $extractRoot | Out-Null

Write-Host "Downloading Tomcat $Version from $downloadUrl"
Invoke-WebRequest -Uri $downloadUrl -OutFile $zipPath

Write-Host "Extracting Tomcat to $extractRoot"
Expand-Archive -Path $zipPath -DestinationPath $extractRoot -Force

if (-not (Test-Path (Join-Path $tomcatHome "bin\startup.bat"))) {
    Write-Error "Tomcat extraction completed, but startup.bat was not found in $tomcatHome"
    exit 1
}

Write-Host "Tomcat installed at $tomcatHome"
