# PowerShell script to fix Java files where import statements appear before package declaration

$sourceDir = "src\main\java"
$javaFiles = Get-ChildItem -Recurse -Path $sourceDir -Filter "*.java"

$filesFixed = 0

foreach ($file in $javaFiles) {
    $content = Get-Content $file.FullName
    if ($content.Length -gt 0) {
        $firstLine = $content[0].Trim()
        
        # Check if first line is an import statement
        if ($firstLine.StartsWith("import ")) {
            Write-Host "Fixing: $($file.FullName)"
            
            # Find the package line
            $packageLineIndex = -1
            for ($i = 0; $i -lt $content.Length; $i++) {
                if ($content[$i].Trim().StartsWith("package ")) {
                    $packageLineIndex = $i
                    break
                }
            }
            
            if ($packageLineIndex -gt 0) {
                # Extract package line
                $packageLine = $content[$packageLineIndex]
                
                # Create new content with package first
                $newContent = @()
                $newContent += $packageLine
                $newContent += ""  # Empty line after package
                
                # Add all lines except the original package line
                for ($i = 0; $i -lt $content.Length; $i++) {
                    if ($i -ne $packageLineIndex) {
                        $newContent += $content[$i]
                    }
                }
                
                # Write back to file
                $newContent | Set-Content $file.FullName -Encoding UTF8
                $filesFixed++
            }
        }
    }
}

Write-Host "Fixed $filesFixed Java files"
