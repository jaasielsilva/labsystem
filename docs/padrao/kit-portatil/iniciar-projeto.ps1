param(
    [Parameter(Mandatory = $true)]
    [string]$Nome,

    [Parameter(Mandatory = $true)]
    [string]$Destino,

    [string]$Kit = $PSScriptRoot
)

$Nome = $Nome.ToLower() -replace '\s+', '-'

$paths = @(
    "$Destino\docs\skills\$Nome",
    "$Destino\.cursor\skills\$Nome"
)

foreach ($p in $paths) {
    New-Item -ItemType Directory -Force -Path $p | Out-Null
}

Copy-Item "$Kit\ARQUITETURA.md" "$Destino\docs\ARQUITETURA.md" -Force
Copy-Item "$Kit\SKILL.md"       "$Destino\docs\skills\$Nome\SKILL.md" -Force
Copy-Item "$Kit\SKILL.md"       "$Destino\.cursor\skills\$Nome\SKILL.md" -Force

Write-Host ""
Write-Host "Kit copiado para: $Destino" -ForegroundColor Green
Write-Host ""
Write-Host "Proximos passos:"
Write-Host "  1. Abra a pasta no Cursor"
Write-Host "  2. Busque '{' nos arquivos e substitua os placeholders"
Write-Host "  3. Use: /$Nome + seu prompt de setup"
Write-Host ""
