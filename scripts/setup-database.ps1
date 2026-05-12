param(
    [string]$MySqlUser = "root",
    [string]$MySqlPassword,
    [string]$MySqlExecutable = "C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe"
)

if (-not (Test-Path $MySqlExecutable)) {
    Write-Error "MySQL executable not found at: $MySqlExecutable"
    exit 1
}

if ([string]::IsNullOrWhiteSpace($MySqlPassword)) {
    $securePassword = Read-Host "Enter MySQL password for user '$MySqlUser'" -AsSecureString
    $bstr = [System.Runtime.InteropServices.Marshal]::SecureStringToBSTR($securePassword)
    $MySqlPassword = [System.Runtime.InteropServices.Marshal]::PtrToStringAuto($bstr)
    [System.Runtime.InteropServices.Marshal]::ZeroFreeBSTR($bstr)
}

$mysqlArgs = @("-u$MySqlUser", "-p$MySqlPassword")

Write-Host "Importing schema.sql..."
Get-Content "database/schema.sql" | & $MySqlExecutable @mysqlArgs
if ($LASTEXITCODE -ne 0) {
    Write-Error "Failed to import schema.sql"
    exit $LASTEXITCODE
}

Write-Host "Importing sample_data.sql..."
Get-Content "database/sample_data.sql" | & $MySqlExecutable @mysqlArgs
if ($LASTEXITCODE -ne 0) {
    Write-Error "Failed to import sample_data.sql"
    exit $LASTEXITCODE
}

Write-Host "Database setup completed successfully."
