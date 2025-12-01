#!/usr/bin/env bash
set -euo pipefail

# Jakarta EE migration guard for Spring Boot 3.x
# Fails when EE javax.* imports are found (servlet/validation/annotation/persistence/xml.bind/jms),
# while allowing JDK javax.* standard libraries (crypto/net/security/naming/xml.parsers/xml.transform).

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT_DIR"

echo "=== Jakarta Guard: scanning EE javax.* and @Autowired usage ==="

violation=0
files_with_forbidden=()

scan_path="smart-admin-api-java17-springboot3"
if [ ! -d "$scan_path" ]; then
  echo "Scan path not found: $scan_path"
  exit 1
fi

while IFS= read -r -d '' f; do
  if grep -q "import javax\." "$f"; then
    # forbid EE namespaces
    if grep -qE "import javax\.(servlet|validation|annotation|persistence|xml\.bind|jms)" "$f"; then
      files_with_forbidden+=("$f")
      violation=1
    fi
  fi
done < <(find "$scan_path" -name "*.java" -print0)

if [ $violation -ne 0 ]; then
  echo "❌ Forbidden EE javax imports detected in:"
  for f in "${files_with_forbidden[@]}"; do
    echo " - $f"
    # show offending lines (head -n for brevity)
    grep -n "import javax\." "$f" | head -5 || true
  done
  echo "Please migrate EE javax.* to jakarta.* as per CLAUDE.md."
  exit 1
fi

# Check @Autowired
if grep -Rnl --include="*.java" "@Autowired" "$scan_path" >/dev/null 2>&1; then
  echo "❌ Detected @Autowired usage. Use @Resource instead."
  grep -Rnl --include="*.java" "@Autowired" "$scan_path" | head -20
  exit 1
fi

echo "✅ Jakarta Guard passed (no EE javax.* or @Autowired found)."


