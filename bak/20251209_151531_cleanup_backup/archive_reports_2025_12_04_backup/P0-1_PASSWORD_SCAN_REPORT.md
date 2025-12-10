# P0-1: Configuration Security Scan Report

> **Scan Date**: 2025-12-02 14:07:51  
> **Scan Status**: Completed  
> **Priority**: P0 - Immediate Action Required  
> **Operation Type**: Read-Only (Safe)

---

## Summary

| Item | Count |
|------|-------|
| Total Files Scanned | 81 |
| Files with Passwords | 44 |
| Total Passwords Found | 97 |

---

## Findings

### File: `microservices\docker\basic-services.yml`

- **Password Count**: 2
- **Details**:
  - Database password: ioedream@2024
  - Nacos password: ioedream@2024

### File: `microservices\docker\business-services.yml`

- **Password Count**: 2
- **Details**:
  - Database password: ioedream@2024
  - Nacos password: ioedream@2024

### File: `microservices\docker\infrastructure.yml`

- **Password Count**: 3
- **Details**:
  - Database password: ioedream@2024
  - Redis password: ioedream@2024
  - Nacos password: ioedream@2024

### File: `microservices\docker\monitoring.yml`

- **Password Count**: 1
- **Details**:
  - Database password: minioadmin

### File: `microservices\ioedream-access-service\src\main\resources\application-seata.yml`

- **Password Count**: 3
- **Details**:
  - Database password: nacos}
  - Redis password: }
  - Nacos password: nacos}

### File: `microservices\ioedream-access-service\src\main\resources\application.yml`

- **Password Count**: 2
- **Details**:
  - Database password: ""
  - Redis password: zkteco3100

### File: `microservices\ioedream-access-service\src\main\resources\bootstrap.yml`

- **Password Count**: 2
- **Details**:
  - Database password: nacos
  - Nacos password: nacos

### File: `microservices\ioedream-attendance-service\src\main\resources\application.yml`

- **Password Count**: 2
- **Details**:
  - Database password: }
  - Redis password: }

### File: `microservices\ioedream-attendance-service\src\main\resources\bootstrap.yml`

- **Password Count**: 2
- **Details**:
  - Database password: nacos}
  - Nacos password: nacos}

### File: `microservices\ioedream-attendance-service\target\classes\application.yml`

- **Password Count**: 2
- **Details**:
  - Database password: }
  - Redis password: }

### File: `microservices\ioedream-attendance-service\target\classes\bootstrap.yml`

- **Password Count**: 2
- **Details**:
  - Database password: nacos}
  - Nacos password: nacos}

### File: `microservices\ioedream-audit-service\src\main\resources\application.yml`

- **Password Count**: 2
- **Details**:
  - Database password: ""
  - Redis password: }

### File: `microservices\ioedream-auth-service\src\main\resources\application.yml`

- **Password Count**: 3
- **Details**:
  - Database password: password1234}
  - Redis password: }
  - Nacos password: password1234}

### File: `microservices\ioedream-common-service\src\main\resources\bootstrap.yml`

- **Password Count**: 3
- **Details**:
  - Database password: nacos}
  - Redis password: }
  - Nacos password: nacos}

### File: `microservices\ioedream-common-service\.gitlab-ci.yml`

- **Password Count**: 2
- **Details**:
  - Database password: "test123456"
  - Redis password: "test123456"

### File: `microservices\ioedream-config-service\src\main\resources\application.yml`

- **Password Count**: 2
- **Details**:
  - Database password: ""
  - Nacos password: admin123

### File: `microservices\ioedream-consume-service\src\main\resources\application.yml`

- **Password Count**: 3
- **Details**:
  - Database password: ""
  - Redis password: zkteco3100
  - Nacos password: nacos}

### File: `microservices\ioedream-device-service\src\main\resources\application.yml`

- **Password Count**: 3
- **Details**:
  - Database password: root1234
  - Redis password: zkteco3100
  - Nacos password: 1024

### File: `microservices\ioedream-device-service\docker-compose.yml`

- **Password Count**: 3
- **Details**:
  - Database password: root1234
  - Redis password: root1234
  - Nacos password: device-service-password

### File: `microservices\ioedream-enterprise-service\src\main\resources\application.yml`

- **Password Count**: 3
- **Details**:
  - Database password: ioedream@2024
  - Redis password: database: 3
  - Nacos password: password

### File: `microservices\ioedream-identity-service\src\main\resources\application.yml`

- **Password Count**: 3
- **Details**:
  - Database password: root1234
  - Redis password: zkteco3100
  - Nacos password: max-attempts: 5  # 鏈€澶у皾璇曟鏁?

### File: `microservices\ioedream-identity-service\docker-compose.yml`

- **Password Count**: 3
- **Details**:
  - Database password: root1234
  - Redis password: root1234
  - Nacos password: root1234

### File: `microservices\ioedream-infrastructure-service\src\main\resources\application.yml`

- **Password Count**: 3
- **Details**:
  - Database password: root
  - Redis password: timeout: 3000ms
  - Nacos password: your-password

### File: `microservices\ioedream-integration-service\k8s\configmap.yaml`

- **Password Count**: 3
- **Details**:
  - Database password: redis123456
  - Redis password: redis123456
  - Nacos password: nacos

### File: `microservices\ioedream-integration-service\k8s\secret.yaml`

- **Password Count**: 1
- **Details**:
  - Database password: aW9lZHJlYW0xMjM=  # ioedream123

### File: `microservices\ioedream-integration-service\src\main\resources\application.yml`

- **Password Count**: 2
- **Details**:
  - Database password: 123456}
  - Redis password: }

### File: `microservices\ioedream-integration-service\src\main\resources\bootstrap.yml`

- **Password Count**: 2
- **Details**:
  - Database password: nacos}
  - Nacos password: nacos}

### File: `microservices\ioedream-integration-service\docker-compose.yml`

- **Password Count**: 3
- **Details**:
  - Database password: root123456
  - Redis password: root123456
  - Nacos password: root123456

### File: `microservices\ioedream-monitor-service\src\main\resources\application.yml`

- **Password Count**: 2
- **Details**:
  - Database password: ""
  - Nacos password: ioedream123

### File: `microservices\ioedream-notification-service\src\main\resources\application.yml`

- **Password Count**: 2
- **Details**:
  - Database password: ""
  - Redis password: }

### File: `microservices\ioedream-oa-service\src\main\resources\application.yml`

- **Password Count**: 2
- **Details**:
  - Database password: 123456}
  - Redis password: }

### File: `microservices\ioedream-oa-service\src\main\resources\bootstrap.yml`

- **Password Count**: 2
- **Details**:
  - Database password: nacos}
  - Nacos password: nacos}

### File: `microservices\ioedream-report-service\src\main\resources\application-simple.yml`

- **Password Count**: 1
- **Details**:
  - Database password: ""

### File: `microservices\ioedream-report-service\src\main\resources\application.yml`

- **Password Count**: 3
- **Details**:
  - Database password: ""
  - Redis password: ioedream123
  - Nacos password: ioedream123

### File: `microservices\ioedream-system-service\src\main\resources\application-simple.yml`

- **Password Count**: 1
- **Details**:
  - Database password: hikari:

### File: `microservices\ioedream-system-service\src\main\resources\application.yml`

- **Password Count**: 3
- **Details**:
  - Database password: 123456
  - Redis password: database: 3
  - Nacos password: 123456

### File: `microservices\ioedream-video-service\src\main\resources\application.yml`

- **Password Count**: 2
- **Details**:
  - Database password: ""
  - Redis password: zkteco3100

### File: `microservices\ioedream-video-service\src\main\resources\bootstrap.yml`

- **Password Count**: 2
- **Details**:
  - Database password: nacos
  - Nacos password: nacos

### File: `microservices\ioedream-visitor-service\src\main\resources\application.yml`

- **Password Count**: 1
- **Details**:
  - Database password: root

### File: `microservices\k8s\helm\ioedream\values.yaml`

- **Password Count**: 3
- **Details**:
  - Database password: "ioedream_root_password"
  - Redis password: "ioedream_redis_password"
  - Nacos password: "ioedream_nacos_password"

### File: `microservices\k8s\k8s-deployments\configmaps\gateway-config.yaml`

- **Password Count**: 2
- **Details**:
  - Database password: }
  - Redis password: }

### File: `microservices\k8s\k8s-deployments\configmaps\microservices-common-config.yaml`

- **Password Count**: 2
- **Details**:
  - Database password: }
  - Redis password: }

### File: `microservices\k8s\k8s-deployments\secrets\database-secrets.yaml`

- **Password Count**: 1
- **Details**:
  - Database password: eW91ci1zdHJvbmctcGFzc3dvcmQ=  # your-strong-password (璇锋浛鎹?

### File: `microservices\monitoring\alertmanager\alertmanager.yml`

- **Password Count**: 1
- **Details**:
  - Database password: 'your-email-password'  # 璇锋浛鎹负瀹為檯瀵嗙爜

---

## Recommended Actions

### Option 1: Use Environment Variables (Recommended)

```yaml
# Before
spring:
  datasource:
    password: "123456"  # Plaintext password

# After
spring:
  datasource:
    password: ${DB_PASSWORD}  # From environment variable
```

### Option 2: Use Nacos Encrypted Configuration (Enterprise)

```yaml
spring:
  cloud:
    nacos:
      config:
        server-addr: ${NACOS_SERVER_ADDR}
        namespace: ${NACOS_NAMESPACE}
```

---

## Next Steps

1. **Review this report**: Understand all password locations
2. **Prepare environment variables**: Create .env file
3. **Manual review**: Confirm which passwords need replacement
4. **Backup before changes**: Always backup configuration files
5. **Replace passwords**: Use automated scripts after review
6. **Verify configuration**: Test service connections

---

**Scan Executed By**: IOE-DREAM Architecture Committee  
**Report Date**: 2025-12-02  
**Report Status**: Completed
