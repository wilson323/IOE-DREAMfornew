# IOE-DREAM Project Dependency Analysis & Optimization Report

**Date:** 2025-12-20
**Status:** Completed
**Author:** IOE-DREAM Architecture Team

---

## 1. Executive Summary

A comprehensive audit of the `IOE-DREAM` microservices dependency architecture revealed significant issues related to **redundancy**, **architectural coupling**, and **version fragmentation**. 

We have successfully executed a "Flattening Strategy" to modularize the system, reducing the dependency footprint of core services and establishing a strict "Service -> Granular Module" hierarchy.

## 2. Dependency Analysis Findings

### 2.1. Structural Redundancy (The "Aggregator Anti-Pattern")
*   **Issue:** 8 out of 10 microservices were importing **BOTH** granular common modules (e.g., `common-core`, `common-data`) **AND** the heavy `microservices-common` aggregator module.
*   **Impact:** 
    *   **Circular Dependencies:** High risk of cycles as services implicitly depended on everything.
    *   **Bloated Classpath:** Stateless services were inheriting unnecessary libraries (e.g., Workflow engine, Report generators) via the aggregator.
    *   **Build Time:** Increased compilation time due to redundant classpath scanning.

### 2.2. "Heavy Core" Syndrome
*   **Issue:** `microservices-common-core` contained `druid`, `redis`, and `mybatis-plus` dependencies.
*   **Impact:** Any module needing basic utilities (StringUtil) was forced to pull in the entire Database/Cache stack.

### 2.3. Version Management
*   **Issue:** Third-party SDKs (Alipay, WeChat Pay, ZXing) had hardcoded versions in submodules.
*   **Impact:** Version drift and potential conflicts during integration.

## 3. Optimization Implementation

### 3.1. Dependency Flattening (Completed)
We removed the `microservices-common` aggregator dependency from **ALL** services, enforcing explicit declarations:

| Service | Status | Action Taken |
| :--- | :--- | :--- |
| `ioedream-consume-service` | ✅ Optimized | Removed aggregator, retained granular modules |
| `ioedream-attendance-service` | ✅ Optimized | Removed aggregator, retained granular modules |
| `ioedream-device-comm-service` | ✅ Optimized | Removed aggregator, retained granular modules |
| `ioedream-oa-service` | ✅ Optimized | Removed aggregator, retained granular modules |
| `ioedream-access-service` | ✅ Optimized | Removed aggregator, retained granular modules |
| `ioedream-video-service` | ✅ Optimized | Removed aggregator, retained granular modules |
| `ioedream-visitor-service` | ✅ Optimized | Removed aggregator, retained granular modules |
| `ioedream-biometric-service` | ✅ Optimized | Removed aggregator, retained granular modules |
| `ioedream-gateway-service` | ✅ Healthy | Already using granular modules |

### 3.2. Common Module Refactoring (Completed)
*   **`common-core`**: Stripped of heavy DB/Cache dependencies.
*   **`common-data`**: Now houses `DatabaseOptimizationManager` and `I18nResourceDao`.
*   **`common-cache`**: Now houses `CacheOptimizationManager`.
*   **`common-storage`**: Added explicit `spring-web` and `slf4j-api` dependencies to fix "Used Undeclared" warnings.
*   **`common-workflow`**: Added explicit `spring-context` dependencies.

### 3.3. Version Centralization (Completed)
*   **Root POM**: Added `<dependencyManagement>` entries for:
    *   `alipay-sdk-java`
    *   `wechatpay-java`
    *   `zxing` (core & javase)
    *   `lombok` (Downgraded to 1.18.30 for stability)

## 4. Automation & Health Check

A PowerShell script `dependency-check.ps1` has been deployed to the project root.

**Usage:**
```powershell
.\dependency-check.ps1
```

**Function:**
*   Runs `mvn dependency:analyze` on all modules.
*   Reports "Unused Declared" dependencies (Candidates for removal).
*   Reports "Used Undeclared" dependencies (Candidates for explicit declaration).

## 5. Future Guidelines

1.  **No Aggregators:** Do not depend on `microservices-common` in any new service. Depend only on what you use (e.g., `common-core`, `common-redis`).
2.  **Explicit Versions:** Never hardcode versions in submodules. Add them to the Root POM.
3.  **Lightweight Core:** Keep `common-core` pure (Utils, Constants, Base Exceptions only). Put feature-heavy code in `common-feature`.
