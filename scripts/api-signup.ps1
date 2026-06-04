# PowerShell 5에서 한글 JSON이 깨지지 않도록 UTF-8 바이트로 전송
param(
    [string]$Email = "test3@test.com",
    [string]$Password = "password1",
    [string]$Nickname = "홍길동",
    [string]$BaseUrl = "http://localhost:8081"
)

$payload = @{
    email    = $Email
    password = $Password
    nickname = $Nickname
} | ConvertTo-Json -Compress

$utf8 = New-Object System.Text.UTF8Encoding $false
$bytes = $utf8.GetBytes($payload)

$response = Invoke-RestMethod -Method Post -Uri "$BaseUrl/api/auth/signup" `
    -ContentType "application/json; charset=utf-8" `
    -Body $bytes

$response | Format-List
