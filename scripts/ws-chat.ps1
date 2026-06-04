# Chatflow WebSocket 간단 테스트 (PowerShell 5.1+)
# 사용 예:
#   .\scripts\ws-chat.ps1 -Token "eyJ..." -RoomId 1 -Action join
#   .\scripts\ws-chat.ps1 -Token "eyJ..." -RoomId 1 -Action send -Content "안녕"
param(
    [Parameter(Mandatory = $true)]
    [string]$Token,
    [int]$RoomId = 1,
    [ValidateSet('join', 'send', 'leave')]
    [string]$Action = 'join',
    [string]$Content = 'PowerShell WS 테스트',
    [string]$WsUrl = 'ws://localhost:8081/ws/chat'
)

$ErrorActionPreference = 'Stop'
Add-Type -AssemblyName System.Net.WebSockets -ErrorAction SilentlyContinue

$uri = [Uri]($WsUrl.TrimEnd('/') + '?token=' + [Uri]::EscapeDataString($Token))
$cts = New-Object System.Threading.CancellationTokenSource
$client = [System.Net.WebSockets.ClientWebSocket]::new()

try {
    $client.ConnectAsync($uri, $cts.Token).GetAwaiter().GetResult()
    Write-Host "Connected: $uri" -ForegroundColor Green

    $payload = switch ($Action) {
        'join'  { @{ type = 'JOIN'; roomId = $RoomId } }
        'send'  { @{ type = 'SEND'; roomId = $RoomId; content = $Content; messageType = 'TEXT' } }
        'leave' { @{ type = 'LEAVE'; roomId = $RoomId } }
    }
    $json = $payload | ConvertTo-Json -Compress
    $bytes = [System.Text.Encoding]::UTF8.GetBytes($json)
    $segment = [ArraySegment[byte]]::new($bytes)
    $client.SendAsync($segment, [System.Net.WebSockets.WebSocketMessageType]::Text, $true, $cts.Token).GetAwaiter().GetResult()
    Write-Host "Sent: $json"

    $buffer = New-Object byte[] 8192
    $recv = [ArraySegment[byte]]::new($buffer)
    $timeout = [TimeSpan]::FromSeconds(3)
    $deadline = [DateTime]::UtcNow.Add($timeout)
    while ([DateTime]::UtcNow -lt $deadline) {
        if ($client.State -ne [System.Net.WebSockets.WebSocketState]::Open) { break }
        $task = $client.ReceiveAsync($recv, $cts.Token)
        if (-not $task.Wait(500)) { continue }
        $result = $task.Result
        if ($result.Count -gt 0) {
            $text = [System.Text.Encoding]::UTF8.GetString($buffer, 0, $result.Count)
            Write-Host "Received: $text" -ForegroundColor Cyan
        }
        if ($result.EndOfMessage) { break }
    }
}
finally {
    if ($client.State -eq [System.Net.WebSockets.WebSocketState]::Open) {
        $client.CloseAsync([System.Net.WebSockets.WebSocketCloseStatus]::NormalClosure, '', $cts.Token).GetAwaiter().GetResult()
    }
    $client.Dispose()
    $cts.Dispose()
}
