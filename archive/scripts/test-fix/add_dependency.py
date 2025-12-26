import re

with open('D:/IOE-DREAM/microservices/ioedream-consume-service/pom.xml', 'r', encoding='utf-8') as f:
    content = f.read()

# 在 common-core 依赖后添加 common-entity 依赖
pattern = r'(<dependency>\s*<groupId>net\.lab1024\.sa</groupId>\s*<artifactId>microservices-common-core</artifactId>[\s\S]*?</dependency>)'
replacement = r'\1\n    <dependency>\n      <groupId>net.lab1024.sa</groupId>\n      <artifactId>microservices-common-entity</artifactId>\n      <version>${project.version}</version>\n    </dependency>'

content = re.sub(pattern, replacement, content, count=1)

with open('D:/IOE-DREAM/microservices/ioedream-consume-service/pom.xml', 'w', encoding='utf-8') as f:
    f.write(content)

print('common-entity 依赖已添加')
