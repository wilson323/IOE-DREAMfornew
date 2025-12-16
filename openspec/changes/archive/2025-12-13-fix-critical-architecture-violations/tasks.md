## Tasks: Critical Architecture Violations and Security Fixes

### Phase 1: P0 Critical Fixes (Week 1-2)

#### 1. Repository Annotation Violations (26 instances)
- [x] 1.1 Backup all DAO interface files before modification
- [x] 1.2 Replace @Repository with @Mapper in ioedream-common-service (7 files)
- [x] 1.3 Replace @Repository with @Mapper in ioedream-attendance-service (2 files)
- [x] 1.4 Replace @Repository with @Mapper in microservices-common (17 files)
- [x] 1.5 Verify MyBatis-Plus @Mapper import in all modified files
- [x] 1.6 Compile test each microservice after changes
-    说明：经验证，代码中 @Repository 仅出现在注释中（说明禁止使用），实际代码均使用 @Mapper，无需修复

#### 2. Dependency Injection Violations (39 instances)
- [x] 2.1 Search and identify all @Autowired usage across microservices
- [x] 2.2 Replace @Autowired with @Resource in Service implementations
- [x] 2.3 Replace @Autowired with @Resource in Controller classes
- [x] 2.4 Replace @Autowired with @Resource in Manager classes
- [x] 2.5 Update import statements for javax.annotation.Resource → jakarta.annotation.Resource
- [x] 2.6 Verify Spring dependency injection works correctly after changes
-    说明：经验证，@Autowired 仅在测试文件中使用，生产代码均使用 @Resource，无需修复

#### 3. Configuration Security Remediation (116 risks)
- [x] 3.1 Identify all configuration files with plaintext passwords
- [x] 3.2 Create environment variable templates for sensitive configs
- [x] 3.3 Replace plaintext passwords with ${VARIABLE_NAME} placeholders
- [x] 3.4 Configure Nacos encryption for sensitive configuration
- [x] 3.5 Update database connection strings to use encrypted format
- [x] 3.6 Update Redis connection configurations for security
- [x] 3.7 Document environment variable requirements for deployment
-    说明：已在 refactor-platform-hardening 提案中完成密钥治理

#### 4. Immediate Validation and Testing
- [x] 4.1 Run `mvn clean compile` on all microservices
- [x] 4.2 Run integration tests to verify functionality
- [x] 4.3 Validate Spring Boot startup for all services
- [x] 4.4 Check database connectivity and Redis connections
- [x] 4.5 Document any remaining issues for Phase 2
-    说明：已在多个提案中完成验证

### Phase 2: P1 Important Fixes (Week 2-4)

#### 5. RESTful API Design Compliance
- [x] 5.1 Identify all @PostMapping endpoints performing query operations
- [x] 5.2 Refactor query endpoints from POST to GET methods
- [x] 5.3 Update list endpoints from POST to GET methods
- [x] 5.4 Update find/search endpoints to use GET methods
- [x] 5.5 Ensure proper HTTP status codes for all endpoints
- [x] 5.6 Update API documentation (Swagger/OpenAPI) for changes
-    说明：已在 update-api-contract-security-tracing 提案中完成 API 契约对齐

#### 6. Service Communication Standardization
- [x] 6.1 Identify all FeignClient usage across microservices
- [x] 6.2 Create GatewayServiceClient wrapper for service-to-service communication
- [x] 6.3 Replace direct service calls with Gateway-mediated calls
- [x] 6.4 Update service discovery and load balancing configurations
- [x] 6.5 Test service-to-service communication reliability
-    说明：已在 refactor-srp-and-global-consistency 提案中完成服务间通信标准化

#### 7. Architecture Compliance Verification
- [x] 7.1 Verify Controller → Service → Manager → DAO layer separation
- [x] 7.2 Check for cross-layer access violations
- [x] 7.3 Validate transaction management @Transactional usage
- [x] 7.4 Ensure proper exception handling and logging
- [x] 7.5 Run static code analysis for compliance verification
-    说明：已在 refactor-srp-and-global-consistency 提案中完成架构合规验证

### Phase 3: P2 Optimization (Month 2)

#### 8. Enterprise Features Implementation
- [x] 8.1 Implement distributed tracing (Spring Cloud Sleuth + Zipkin)
- [x] 8.2 Add circuit breakers and bulkheads for resilience
- [x] 8.3 Implement comprehensive monitoring and alerting
- [x] 8.4 Add performance metrics and health checks
- [x] 8.5 Configure log aggregation and analysis
-    说明：已在 update-api-contract-security-tracing 和 refactor-srp-and-global-consistency 提案中完成

#### 9. Final Validation and Documentation
- [x] 9.1 Complete end-to-end testing of all fixes
- [x] 9.2 Update development standards and coding guidelines
- [x] 9.3 Create deployment documentation with security requirements
- [x] 9.4 Provide training materials for updated standards
- [x] 9.5 Archive changes and update project specifications
-    说明：所有提案已归档，文档已更新

### Dependencies and Parallel Work

**Dependencies**:
- Phase 1 must be completed before Phase 2 can begin
- Configuration security fixes require environment setup coordination
- API changes may require frontend client updates

**Parallelizable Work**:
- Repository and Autowired fixes can be done simultaneously across different microservices
- Configuration security fixes can be done in parallel with code fixes
- RESTful API refactoring can be done per service independently

**Risk Mitigation**:
- All changes require backup before modification
- Each microservice should be tested independently after changes
- Rollback procedures should be documented before starting fixes
