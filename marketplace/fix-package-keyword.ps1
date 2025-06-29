# PowerShell script to fix corrupted package declarations

$sourceDir = "src\main\java"
$javaFiles = Get-ChildItem -Recurse -Path $sourceDir -Filter "*.java"

$filesFixed = 0

foreach ($file in $javaFiles) {
    $content = Get-Content $file.FullName -Raw
    if ($content -and $content.StartsWith("ackage ")) {
        Write-Host "Fixing package declaration in: $($file.FullName)"
        $content = $content -replace "^ackage ", "package "
        [System.IO.File]::WriteAllText($file.FullName, $content, [System.Text.UTF8Encoding]::new($false))
        $filesFixed++
    }
}

Write-Host "Fixed package declarations in $filesFixed Java files"
