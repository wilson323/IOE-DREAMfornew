# 创建Maven settings.xml配置文件（使用阿里云镜像加速）

$settingsContent = @"
<?xml version="1.0" encoding="UTF-8"?>
<settings xmlns="http://maven.apache.org/SETTINGS/1.2.0"
          xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
          xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.2.0 
          http://maven.apache.org/xsd/settings-1.2.0.xsd">
  <mirrors>
    <mirror>
      <id>aliyunmaven</id>
      <mirrorOf>*</mirrorOf>
      <name>阿里云公共仓库</name>
      <url>https://maven.aliyun.com/repository/public</url>
    </mirror>
  </mirrors>
  <profiles>
    <profile>
      <id>aliyun</id>
      <repositories>
        <repository>
          <id>aliyun-central</id>
          <name>Aliyun Central</name>
          <url>https://maven.aliyun.com/repository/central</url>
          <releases>
            <enabled>true</enabled>
          </releases>
          <snapshots>
            <enabled>false</enabled>
          </snapshots>
        </repository>
        <repository>
          <id>aliyun-public</id>
          <name>Aliyun Public</name>
          <url>https://maven.aliyun.com/repository/public</url>
          <releases>
            <enabled>true</enabled>
          </releases>
          <snapshots>
            <enabled>false</enabled>
          </snapshots>
        </repository>
      </repositories>
      <pluginRepositories>
        <pluginRepository>
          <id>aliyun-plugin</id>
          <name>Aliyun Plugin</name>
          <url>https://maven.aliyun.com/repository/public</url>
          <releases>
            <enabled>true</enabled>
          </releases>
          <snapshots>
            <enabled>false</enabled>
          </snapshots>
        </pluginRepository>
      </pluginRepositories>
    </profile>
  </profiles>
  <activeProfiles>
    <activeProfile>aliyun</activeProfile>
  </activeProfiles>
</settings>
"@

$settingsPath = "maven-settings.xml"
$settingsContent | Out-File -FilePath $settingsPath -Encoding UTF8 -NoNewline

Write-Host "✅ Maven settings.xml已创建: $settingsPath" -ForegroundColor Green
Write-Host ""
Write-Host "使用方法:" -ForegroundColor Yellow
Write-Host "  1. 在Dockerfile中添加: COPY maven-settings.xml /root/.m2/settings.xml" -ForegroundColor White
Write-Host "  2. 或者在构建时使用: mvn -s maven-settings.xml ..." -ForegroundColor White
