#!/bin/bash

echo "=== Fixing javax to jakarta imports ==="
echo "Date: $(date)"
echo ""

# Find all Java files and replace javax imports with jakarta
find . -name "*.java" -type f | while read file; do
    # Replace javax.persistence with jakarta.persistence
    sed -i 's/import javax\.persistence\./import jakarta.persistence./g' "$file"
    
    # Replace javax.validation with jakarta.validation
    sed -i 's/import javax\.validation\./import jakarta.validation./g' "$file"
    
    # Replace javax.transaction with jakarta.transaction
    sed -i 's/import javax\.transaction\./import jakarta.transaction./g' "$file"
    
    # Count changes
    if grep -q "jakarta\." "$file"; then
        echo "✅ Updated imports in: $file"
    fi
done

echo ""
echo "Import fixes completed at: $(date)"