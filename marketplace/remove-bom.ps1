# PowerShell script to remove BOM characters from Java files

$sourceDir = "src\main\java"
$javaFiles = Get-ChildItem -Recurse -Path $sourceDir -Filter "*.java"

$filesFixed = 0

foreach ($file in $javaFiles) {
    $content = Get-Content $file.FullName -Raw -Encoding UTF8
    if ($content -and $content.StartsWith([char]0xFEFF)) {
        Write-Host "Removing BOM from: $($file.FullName)"
        $content = $content.Substring(1)
        [System.IO.File]::WriteAllText($file.FullName, $content, [System.Text.UTF8Encoding]::new($false))
        $filesFixed++
    }
}

Write-Host "Removed BOM from $filesFixed Java files"
