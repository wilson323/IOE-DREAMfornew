# Tasks: Refactor microservices-common Module

## 1. Preparation

- [x] 1.1 Create new module directories
- [x] 1.2 Configure parent POM with new modules
- [x] 1.3 Create POM files for each new module

## 2. Create microservices-common-security Module

- [x] 2.1 Create module structure and POM
- [x] 2.2 Migrate auth package (26 files)
- [x] 2.3 Migrate rbac package (8 files)
- [x] 2.4 Migrate identity package (8 files)
- [x] 2.5 Migrate audit package (20 files)
- [x] 2.6 Update imports and dependencies

## 3. Create microservices-common-monitor Module

- [x] 3.1 Create module structure and POM
- [x] 3.2 Migrate monitor package (29 files)
- [x] 3.3 Migrate monitoring package (5 files)
- [x] 3.4 Migrate performance package (15 files)
- [x] 3.5 Update imports and dependencies

## 4. Create microservices-common-business Module

- [x] 4.1 Create module structure and POM
- [x] 4.2 Migrate organization package (16 files)
- [x] 4.3 Migrate notification package (17 files)
- [x] 4.4 Migrate menu package (6 files)
- [x] 4.5 Migrate dict package (5 files)
- [x] 4.6 Migrate theme package (6 files)
- [x] 4.7 Migrate scheduler package (3 files)
- [x] 4.8 Migrate system package (21 files)
- [x] 4.9 Update imports and dependencies

## 5. Migrate Business-Specific Code to Services

- [x] 5.1 Migrate workflow to ioedream-oa-service (24 files)
- [x] 5.2 Migrate consume to ioedream-consume-service (19 files)
- [x] 5.3 Migrate visitor to ioedream-visitor-service (6 files)
- [x] 5.4 Migrate access to ioedream-access-service
- [x] 5.5 Migrate attendance to ioedream-attendance-service
- [x] 5.6 Migrate device to ioedream-device-comm-service
- [x] 5.7 Clean up original microservices-common module (0 files remaining)

## 6. Update All Microservices

- [x] 6.1 Update microservices-common to aggregate new modules

## 7. Verification

- [x] 7.1 Run Maven compile for all modules
- [ ] 7.2 Run all unit tests
- [x] 7.3 Verify no circular dependencies
- [x] 7.4 Update documentation
