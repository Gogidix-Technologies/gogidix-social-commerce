$content = Get-Content "pom.xml" -Raw
$content = $content -replace "<n>order-service</n>", "<n>order-service</n>"
$content | Out-File -encoding UTF8 "pom.xml"