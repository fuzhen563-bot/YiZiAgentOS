param($PromptFile)
$ErrorActionPreference = 'Stop'
$outFile = [System.IO.Path]::ChangeExtension($PromptFile, '.out')
try {
    $body = Get-Content $PromptFile -Raw -Encoding UTF8
    $r = Invoke-RestMethod -Uri 'https://api.yiziyun.com/v1/chat/completions' -Method Post -ContentType 'application/json; charset=utf-8' -Headers @{Authorization='Bearer sk-2igVg3QbH8AOHVjuixJR7uDGTUwimoZ1BSjCNwYFTe3toLFa'} -Body $body -TimeoutSec 60
    $text = $r.choices[0].message.content
    [System.IO.File]::WriteAllText($outFile, $text, [System.Text.Encoding]::UTF8)
} catch {
    [System.IO.File]::WriteAllText($outFile, "LLM_ERROR: $_", [System.Text.Encoding]::UTF8)
}