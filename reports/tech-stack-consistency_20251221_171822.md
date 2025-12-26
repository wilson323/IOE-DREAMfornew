# IOE-DREAM 技术栈统一性检查报告

**生成时间**: 2025-12-21 17:18:21
**总体评分**: 88/100
**总体状态**: good

## 1. Jakarta EE迁移状态

- **状态**: complete
- **总文件数**: 1417
- **已迁移**: 338 (23.85%)
- **违规数**: 0

✅ **迁移完成**


## 2. MyBatis-Plus迁移状态

- **状态**: incomplete
- **总DAO文件数**: 79
- **违规数**: 6

### 需要修复的文件

| 文件 | 问题 | 建议 |
|------|------|------|
 | microservices\ioedream-access-service\src\main\java\net\lab1024\sa\access\dao\AccessDeviceDao.java | 使用@Repository注解 | 应使用@Mapper注解并继承BaseMapper |
 | microservices\ioedream-biometric-service\src\main\java\net\lab1024\sa\biometric\dao\BiometricTemplateDao.java | 使用@Repository注解 | 应使用@Mapper注解并继承BaseMapper |
 | microservices\ioedream-oa-service\src\main\java\net\lab1024\sa\oa\workflow\dao\WorkflowDefinitionDao.java | 使用@Repository注解 | 应使用@Mapper注解并继承BaseMapper |
 | microservices\ioedream-oa-service\src\main\java\net\lab1024\sa\oa\workflow\form\FormInstanceDao.java | 使用@Repository注解 | 应使用@Mapper注解并继承BaseMapper |
 | microservices\ioedream-oa-service\src\main\java\net\lab1024\sa\oa\workflow\form\FormSchemaDao.java | 使用@Repository注解 | 应使用@Mapper注解并继承BaseMapper |
 | microservices\microservices-common-business\src\main\java\net\lab1024\sa\common\organization\dao\DeviceDao.java | 使用@Repository注解 | 应使用@Mapper注解并继承BaseMapper |


## 3. 连接池统一性

- **状态**: consistent
- **使用Druid的服务**: 3
- **使用HikariCP的服务**: 0
- **违规数**: 0

✅ **统一使用Druid**


## 4. 依赖注入统一性

- **状态**: consistent
- **使用@Resource**: 91 个文件
- **使用@Autowired**: 0 个文件
- **违规数**: 0

✅ **统一使用@Resource**


## 迁移优先级建议

1. **P0 - 立即修复**: 连接池不统一、MyBatis-Plus迁移
2. **P1 - 短期修复**: Jakarta EE迁移
3. **P2 - 持续优化**: 依赖注入统一

---

**报告文件**: 	ech-stack-consistency_20251221_171822.json

