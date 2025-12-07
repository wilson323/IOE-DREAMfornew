# 批量移除子模块中Sleuth依赖的版本号
# 让子模块从父POM继承版本管理

$ErrorActionPreference = "Stop"

$files = Get-ChildItem -Path "D:\IOE-DREAM\microservices\ioedream-*-service" -Filter "pom.xml" -Recurse |
    Where-Object { $_.DirectoryName -notmatch "target" }

Write-Host "===== Remove Sleuth Version from Services =====" -ForegroundColor Cyan
Write-Host "Found $($files.Count) service pom.xml files`n" -ForegroundColor Yellow

$fixedCount = 0

foreach ($file in $files) {
    try {
        Write-Host "Processing: $($file.Directory.Name)" -ForegroundColor Gray

        [xml]$pom = Get-Content $file.FullName -Encoding UTF8
        $changed = $false

        # 查找并移除Sleuth依赖的version节点
        $dependencies = $pom.project.dependencies.dependency

        foreach ($dep in $dependencies) {
            if ($dep.artifactId -eq "spring-cloud-starter-sleuth" -or
                $dep.artifactId -eq "spring-cloud-sleuth-zipkin" -or
                $dep.artifactId -eq "micrometer-tracing-bridge-brave" -or
                $dep.artifactId -eq "zipkin-reporter-brave") {

                if ($dep.version) {
                    # 移除version节点
                    $dep.RemoveChild($dep.version) | Out-Null
                    Write-Host "  Removed version from $($dep.artifactId)" -ForegroundColor Green
                    $changed = $true
                }
            }
        }

        if ($changed) {
            $pom.Save($file.FullName)
            $fixedCount++
        } else {
            Write-Host "  No change needed" -ForegroundColor DarkGray
        }

    } catch {
        Write-Host "  Failed: $_" -ForegroundColor Red
    }
}

Write-Host "`n===== Fix Complete =====" -ForegroundColor Cyan
Write-Host "Fixed: $fixedCount files" -ForegroundColor Green

