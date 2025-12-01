# HutoL使用规范（权威文档）

> **📋 文档版本**: v4.0.0 (整合版)
> **📋 文档职责**: SmartAdmin项目的唯一HutoL工具库使用规范权威来源，基于HutoL 5.8.39，贴合项目实际情况。

## ⚠️ HutoL使用铁律（不可违反）

### 🚫 绝对禁止
```markdown
❌ 禁止使用HutoL的DateUtil（使用LocalDateTime替代）
❌ 禁止使用HutoL的BeanUtil.copyProperties（使用SmartBeanUtil.copy）
❌ 禁止使用HutoL的CollUtil.isEmpty（使用CollUtil.isEmpty）
❌ 禁止使用HutoL的StrUtil.isBlank（使用StrUtil.isNotBlank）
❌ 禁止使用HutoL的JSONUtil.parseObj（使用SmartJsonUtil.parseObject）
❌ 禁止使用HutoL的CryptoUtil（使用项目自带的加密工具）
❌ 禁止使用HutoL的FileUtil（避免文件操作安全问题）
❌ 禁止使用HutoL的HttpUtil（使用项目自带的HTTP工具）
```

### ✅ 必须执行
```markdown
✅ 必须使用HutoL的NumberUtil进行数字处理
✅ 必须使用HutoL的ValidatorUtil进行参数验证
✅ 必须使用HutoL的IdUtil生成ID
✅ 必须使用HutoL的ReUtil进行正则表达式处理
✅ 必须使用HutoL的RandomUtil生成随机数
✅ 必须使用HutoL的DigestUtil进行摘要计算
✅ 必须使用HutoL的ZipUtil进行压缩处理
✅ 必须使用HutoL的ImgUtil进行图片处理
```

## 📚 HutoL工具类分类使用规范

### 🔢 数字处理工具类
```java
// NumberUtil使用规范
public class NumberUtils {

    /**
     * 金额相加（避免精度问题）
     */
    public static BigDecimal addAmount(BigDecimal amount1, BigDecimal amount2) {
        return NumberUtil.add(amount1, amount2);
    }

    /**
     * 金额格式化
     */
    public static String formatAmount(BigDecimal amount) {
        return NumberUtil.decimalFormat(",###.##", amount);
    }

    /**
     * 百分比计算
     */
    public static String calculatePercentage(Long numerator, Long denominator) {
        if (denominator == null || denominator == 0) {
            return "0%";
        }
        double percentage = NumberUtil.mul(numerator, 100.0) / denominator;
        return NumberUtil.round(percentage, 2) + "%";
    }

    /**
     * 安全的整数转换
     */
    public static Integer safeParseInt(String value) {
        return NumberUtil.parseInt(value, 0); // 带默认值
    }

    /**
     * 安全的长整数转换
     */
    public static Long safeParseLong(String value) {
        return NumberUtil.parseLong(value, 0L); // 带默认值
    }

    /**
     * 范围检查
     */
    public static boolean inRange(Integer value, Integer min, Integer max) {
        return NumberUtil.isBetween(value, min, max);
    }
}
```

### ✅ 日期时间工具类（使用LocalDateTime）
```java
// DateTimeUtil使用规范（基于LocalDateTime）
public class DateTimeUtils {

    /**
     * 获取当前时间
     */
    public static LocalDateTime now() {
        return LocalDateTime.now();
    }

    /**
     * 格式化时间
     */
    public static String format(LocalDateTime dateTime, String pattern) {
        if (dateTime == null) {
            return "";
        }
        return DateTimeFormatter.ofPattern(pattern).format(dateTime);
    }

    /**
     * 解析时间字符串
     */
    public static LocalDateTime parse(String dateStr, String pattern) {
        if (StrUtil.isBlank(dateStr)) {
            return null;
        }
        try {
            return LocalDateTime.parse(dateStr, DateTimeFormatter.ofPattern(pattern));
        } catch (Exception e) {
            log.warn("时间解析失败：dateStr={}, pattern={}", dateStr, pattern, e);
            return null;
        }
    }

    /**
     * 时间加减
     */
    public static LocalDateTime plusDays(LocalDateTime dateTime, long days) {
        return dateTime != null ? dateTime.plusDays(days) : null;
    }

    /**
     * 计算时间差（分钟）
     */
    public static long betweenMinutes(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null) {
            return 0L;
        }
        return Duration.between(start, end).toMinutes();
    }

    /**
     * 获取当天开始时间
     */
    public static LocalDateTime getStartOfDay(LocalDateTime dateTime) {
        return dateTime != null ? dateTime.with(LocalTime.MIN) : null;
    }

    /**
     * 获取当天结束时间
     */
    public static LocalDateTime getEndOfDay(LocalDateTime dateTime) {
        return dateTime != null ? dateTime.with(LocalTime.MAX) : null;
    }

    /**
     * 获取本月第一天
     */
    public static LocalDateTime getFirstDayOfMonth(LocalDateTime dateTime) {
        return dateTime != null ? dateTime.withDayOfMonth(1).with(LocalTime.MIN) : null;
    }

    /**
     * 获取本月最后一天
     */
    public static LocalDateTime getLastDayOfMonth(LocalDateTime dateTime) {
        return dateTime != null ? dateTime.withDayOfMonth(dateTime.toLocalDate().lengthOfMonth()).with(LocalTime.MAX) : null;
    }
}
```

### 🔤 字符串工具类
```java
// StrUtil使用规范
public class StringUtils {

    /**
     * 判断字符串是否为空
     */
    public static boolean isEmpty(String str) {
        return StrUtil.isEmpty(str);
    }

    /**
     * 判断字符串是否不为空
     */
    public static boolean isNotEmpty(String str) {
        return StrUtil.isNotEmpty(str);
    }

    /**
     * 判断字符串是否为空白（包含空格、制表符等）
     */
    public static boolean isBlank(String str) {
        return StrUtil.isBlank(str);
    }

    /**
     * 判断字符串是否不为空白
     */
    public static boolean isNotBlank(String str) {
        return StrUtil.isNotBlank(str);
    }

    /**
     * 字符串脱敏
     */
    public static String desensitize(String str, int start, int end, String replaceChar) {
        if (StrUtil.isBlank(str)) {
            return str;
        }
        return StrUtil.hide(str, start, end, replaceChar);
    }

    /**
     * 手机号脱敏
     */
    public static String desensitizePhone(String phone) {
        return desensitize(phone, 3, 7, "****");
    }

    /**
     * 邮箱脱敏
     */
    public static String desensitizeEmail(String email) {
        if (StrUtil.isBlank(email) || !email.contains("@")) {
            return email;
        }
        String[] parts = email.split("@");
        String username = parts[0];
        String domain = parts[1];
        if (username.length() <= 2) {
            return email;
        }
        String desensitizedUsername = StrUtil.hide(username, 1, username.length() - 1, "*");
        return desensitizedUsername + "@" + domain;
    }

    /**
     * 首字母大写
     */
    public static String capitalize(String str) {
        return StrUtil.upperFirst(str);
    }

    /**
     * 驼峰转下划线
     */
    public static String camelToUnderscore(String str) {
        return StrUtil.toUnderlineCase(str);
    }

    /**
     * 下划线转驼峰
     */
    public static String underscoreToCamel(String str) {
        return StrUtil.toCamelCase(str);
    }

    /**
     * 随机字符串
     */
    public static String randomString(int length) {
        return RandomUtil.randomString(length);
    }

    /**
     * 生成UUID
     */
    public static String generateUUID() {
        return IdUtil.fastSimpleUUID();
    }
}
```

### 📦 集合工具类
```java
// CollUtil使用规范
public class CollectionUtils {

    /**
     * 判断集合是否为空
     */
    public static boolean isEmpty(Collection<?> collection) {
        return CollUtil.isEmpty(collection);
    }

    /**
     * 判断集合是否不为空
     */
    public static boolean isNotEmpty(Collection<?> collection) {
        return CollUtil.isNotEmpty(collection);
    }

    /**
     * 集合分页
     */
    public static <T> List<T> page(List<T> list, int page, int size) {
        if (isEmpty(list)) {
            return new ArrayList<>();
        }
        return CollUtil.page(page - 1, size, list);
    }

    /**
     * 集合去重
     */
    public static <T> List<T> distinct(List<T> list) {
        return CollUtil.distinct(list);
    }

    /**
     * 集合分组
     */
    public static <T, K> Map<K, List<T>> groupBy(List<T> list, Function<T, K> keyExtractor) {
        if (isEmpty(list)) {
            return new HashMap<>();
        }
        return list.stream().collect(Collectors.groupingBy(keyExtractor));
    }

    /**
     * 从集合中提取属性
     */
    public static <T, R> List<R> extractProperty(List<T> list, Function<T, R> propertyExtractor) {
        if (isEmpty(list)) {
            return new ArrayList<>();
        }
        return list.stream().map(propertyExtractor).filter(Objects::nonNull).collect(Collectors.toList());
    }

    /**
     * 安全的获取集合元素
     */
    public static <T> T safeGet(List<T> list, int index) {
        return CollUtil.get(list, index);
    }

    /**
     * 数组转List
     */
    public static <T> List<T> arrayToList(T[] array) {
        return CollUtil.newArrayList(array);
    }

    /**
     * 交集操作
     */
    public static <T> List<T> intersection(Collection<T> coll1, Collection<T> coll2) {
        return CollUtil.intersection(coll1, coll2);
    }

    /**
     * 并集操作
     */
    public static <T> List<T> union(Collection<T> coll1, Collection<T> coll2) {
        return CollUtil.union(coll1, coll2);
    }
}
```

### 🎯 JSON工具类
```java
// SmartJsonUtil使用规范（基于HutoL JSONUtil的增强版）
public class SmartJsonUtil {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    static {
        // 配置ObjectMapper
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        objectMapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
        objectMapper.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"));
    }

    /**
     * 对象转JSON字符串
     */
    public static String toJsonString(Object obj) {
        if (obj == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            log.error("对象转JSON失败", e);
            return null;
        }
    }

    /**
     * JSON字符串转对象
     */
    public static <T> T parseObject(String json, Class<T> clazz) {
        if (StrUtil.isBlank(json)) {
            return null;
        }
        try {
            return objectMapper.readValue(json, clazz);
        } catch (Exception e) {
            log.error("JSON转对象失败：json={}, clazz={}", json, clazz.getName(), e);
            return null;
        }
    }

    /**
     * JSON字符串转List
     */
    public static <T> List<T> parseArray(String json, Class<T> clazz) {
        if (StrUtil.isBlank(json)) {
            return new ArrayList<>();
        }
        try {
            JavaType javaType = objectMapper.getTypeFactory()
                    .constructParametricType(List.class, clazz);
            return objectMapper.readValue(json, javaType);
        } catch (Exception e) {
            log.error("JSON转List失败：json={}, clazz={}", json, clazz.getName(), e);
            return new ArrayList<>();
        }
    }

    /**
     * JSON字符串转Map
     */
    public static Map<String, Object> parseMap(String json) {
        if (StrUtil.isBlank(json)) {
            return new HashMap<>();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            log.error("JSON转Map失败：json={}", json, e);
            return new HashMap<>();
        }
    }

    /**
     * 对象转Map
     */
    public static Map<String, Object> objectToMap(Object obj) {
        if (obj == null) {
            return new HashMap<>();
        }
        try {
            return objectMapper.convertValue(obj, new TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            log.error("对象转Map失败", e);
            return new HashMap<>();
        }
    }

    /**
     * Map转对象
     */
    public static <T> T mapToObject(Map<String, Object> map, Class<T> clazz) {
        if (CollUtil.isEmpty(map)) {
            return null;
        }
        try {
            return objectMapper.convertValue(map, clazz);
        } catch (Exception e) {
            log.error("Map转对象失败：map={}, clazz={}", map, clazz.getName(), e);
            return null;
        }
    }
}
```

### 🔄 Bean工具类
```java
// SmartBeanUtil使用规范（基于HutoL BeanUtil的增强版）
public class SmartBeanUtil {

    private static final BeanCopier copier = BeanCopier.create();

    /**
     * 对象拷贝
     */
    public static <T> T copy(Object source, Class<T> targetClass) {
        if (source == null) {
            return null;
        }
        try {
            T target = targetClass.getDeclaredConstructor().newInstance();
            copier.copyProperties(source, target, CopyOptions.create().ignoreError());
            return target;
        } catch (Exception e) {
            log.error("对象拷贝失败：source={}, targetClass={}", source.getClass().getName(), targetClass.getName(), e);
            return null;
        }
    }

    /**
     * 对象拷贝（指定忽略字段）
     */
    public static <T> T copy(Object source, Class<T> targetClass, String... ignoreProperties) {
        if (source == null) {
            return null;
        }
        try {
            T target = targetClass.getDeclaredConstructor().newInstance();
            CopyOptions options = CopyOptions.create().ignoreError().ignoreProperties(ignoreProperties);
            copier.copyProperties(source, target, options);
            return target;
        } catch (Exception e) {
            log.error("对象拷贝失败：source={}, targetClass={}", source.getClass().getName(), targetClass.getName(), e);
            return null;
        }
    }

    /**
     * 列表拷贝
     */
    public static <T> List<T> copyList(List<?> sourceList, Class<T> targetClass) {
        if (CollUtil.isEmpty(sourceList)) {
            return new ArrayList<>();
        }
        List<T> targetList = new ArrayList<>(sourceList.size());
        for (Object source : sourceList) {
            T target = copy(source, targetClass);
            if (target != null) {
                targetList.add(target);
            }
        }
        return targetList;
    }

    /**
     * 对象属性拷贝到已有对象
     */
    public static void copyProperties(Object source, Object target) {
        if (source == null || target == null) {
            return;
        }
        try {
            copier.copyProperties(source, target, CopyOptions.create().ignoreError());
        } catch (Exception e) {
            log.error("属性拷贝失败：source={}, target={}", source.getClass().getName(), target.getClass().getName(), e);
        }
    }

    /**
     * 转换为Map
     */
    public static Map<String, Object> beanToMap(Object bean) {
        if (bean == null) {
            return new HashMap<>();
        }
        try {
            return BeanUtil.beanToMap(bean, false, true);
        } catch (Exception e) {
            log.error("Bean转Map失败", e);
            return new HashMap<>();
        }
    }

    /**
     * Map转Bean
     */
    public static <T> T mapToBean(Map<String, Object> map, Class<T> beanClass) {
        if (CollUtil.isEmpty(map)) {
            return null;
        }
        try {
            return BeanUtil.mapToBean(map, beanClass, false);
        } catch (Exception e) {
            log.error("Map转Bean失败：map={}, beanClass={}", map, beanClass.getName(), e);
            return null;
        }
    }
}
```

### 🎲 随机工具类
```java
// RandomUtil使用规范
public class RandomUtils {

    /**
     * 生成随机数字字符串
     */
    public static String randomNumbers(int length) {
        return RandomUtil.randomNumbers(length);
    }

    /**
     * 生成随机字母字符串
     */
    public static String randomString(int length) {
        return RandomUtil.randomString(length);
    }

    /**
     * 生成随机字符串（数字+字母）
     */
    public static String randomString(String baseString, int length) {
        return RandomUtil.randomString(baseString, length);
    }

    /**
     * 生成随机整数
     */
    public static int randomInt(int min, int max) {
        return RandomUtil.randomInt(min, max);
    }

    /**
     * 生成随机长整数
     */
    public static long randomLong(long min, long max) {
        return RandomUtil.randomLong(min, max);
    }

    /**
     * 生成随机BigDecimal
     */
    public static BigDecimal randomBigDecimal(BigDecimal min, BigDecimal max) {
        return RandomUtil.randomBigDecimal(min, max);
    }

    /**
     * 生成随机UUID
     */
    public static String randomUUID() {
        return IdUtil.fastSimpleUUID();
    }

    /**
     * 生成雪花算法ID
     */
    public static long snowflakeId() {
        return IdUtil.getSnowflakeNextId();
    }

    /**
     * 生成ObjectId
     */
    public static String objectId() {
        return IdUtil.objectId();
    }

    /**
     * 生成随机颜色
     */
    public static String randomColor() {
        return RandomUtil.randomColor();
    }
}
```

### 🔍 正则表达式工具类
```java
// ReUtil使用规范
public class RegexUtils {

    /**
     * 验证手机号
     */
    public static boolean isPhone(String phone) {
        String regex = "^1[3-9]\\d{9}$";
        return ReUtil.isMatch(regex, phone);
    }

    /**
     * 验证邮箱
     */
    public static boolean isEmail(String email) {
        String regex = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";
        return ReUtil.isMatch(regex, email);
    }

    /**
     * 验证身份证号
     */
    public static boolean isIdCard(String idCard) {
        String regex = "(^\\d{15}$)|(^\\d{18}$)|(^\\d{17}(\\d|X|x)$)";
        return ReUtil.isMatch(regex, idCard);
    }

    /**
     * 验证密码强度（8-20位，包含大小写字母、数字、特殊字符）
     */
    public static boolean isStrongPassword(String password) {
        String regex = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,20}$";
        return ReUtil.isMatch(regex, password);
    }

    /**
     * 验证URL
     */
    public static boolean isUrl(String url) {
        String regex = "^(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]";
        return ReUtil.isMatch(regex, url);
    }

    /**
     * 验证IPv4地址
     */
    public static boolean isIPv4(String ip) {
        String regex = "^((25[0-5]|2[0-4]\\d|[01]?\\d\\d?)\\.){3}(25[0-5]|2[0-4]\\d|[01]?\\d\\d?)$";
        return ReUtil.isMatch(regex, ip);
    }

    /**
     * 提取数字
     */
    public static List<String> extractNumbers(String text) {
        return ReUtil.findAllGroup1("\\d+", text);
    }

    /**
     * 提取中文
     */
    public static List<String> extractChinese(String text) {
        return ReUtil.findAllGroup1("[\\u4e00-\\u9fa5]+", text);
    }

    /**
     * 替换敏感词
     */
    public static String replaceSensitiveWords(String text, List<String> sensitiveWords, String replacement) {
        if (StrUtil.isBlank(text) || CollUtil.isEmpty(sensitiveWords)) {
            return text;
        }

        String result = text;
        for (String word : sensitiveWords) {
            result = result.replaceAll(word, replacement);
        }
        return result;
    }

    /**
     * 获取匹配组
     */
    public static String getGroup(String text, String regex, int groupIndex) {
        return ReUtil.getGroup1(regex, text);
    }
}
```

### 📐 验证工具类
```java
// ValidatorUtil使用规范
public class ValidationUtils {

    /**
     * 验证非空
     */
    public static boolean isNotNull(Object obj) {
        return Validator.isNotNull(obj);
    }

    /**
     * 验证非空（字符串）
     */
    public static boolean isNotEmpty(String str) {
        return Validator.isNotEmpty(str);
    }

    /**
     * 验证数字
     */
    public static boolean isNumber(String str) {
        return Validator.isNumber(str);
    }

    /**
     * 验证邮箱
     */
    public static boolean isEmail(String email) {
        return Validator.isEmail(email);
    }

    /**
     * 验证手机号
     */
    public static boolean isMobile(String mobile) {
        return Validator.isMobile(mobile);
    }

    /**
     * 验证身份证号
     */
    public static boolean isCitizenId(String citizenId) {
        return Validator.isCitizenId(citizenId);
    }

    /**
     * 验证URL
     */
    public static boolean isUrl(String url) {
        return Validator.isUrl(url);
    }

    /**
     * 验证汉字
     */
    public static boolean isChinese(String str) {
        return Validator.isChinese(str);
    }

    /**
     * 验证字母
     */
    public static boolean isLetter(String str) {
        return Validator.isLetter(str);
    }

    /**
     * 验证大写字母
     */
    public static boolean isUpperCase(String str) {
        return Validator.isUpperCase(str);
    }

    /**
     * 验证小写字母
     */
    public static boolean isLowerCase(String str) {
        return Validator.isLowerCase(str);
    }

    /**
     * 验证日期
     */
    public static boolean isDate(String date) {
        return Validator.isDate(date);
    }

    /**
     * 验证日期时间
     */
    public static boolean isDateTime(String dateTime) {
        return Validator.isDateTime(dateTime);
    }
}
```

## 🔧 常用工具类集合

### 文件工具类
```java
// FileUtil使用规范（安全版本）
public class FileUtils {

    /**
     * 安全的读取文件内容
     */
    public static String readUtf8String(String filePath) throws IOException {
        File file = new File(filePath);
        if (!file.exists() || !file.isFile()) {
            throw new FileNotFoundException("文件不存在：" + filePath);
        }
        // 限制文件大小，避免读取大文件
        if (file.length() > 10 * 1024 * 1024) { // 10MB
            throw new IOException("文件过大：" + filePath);
        }
        return FileUtil.readUtf8String(file);
    }

    /**
     * 安全的写入文件内容
     */
    public static void writeUtf8String(String content, String filePath) throws IOException {
        // 验证文件路径安全性
        Path path = Paths.get(filePath).normalize();
        if (!path.startsWith(Paths.get("/safe/uploads"))) { // 限制写入目录
            throw new IOException("不安全的文件路径：" + filePath);
        }

        // 限制内容大小
        if (content.length() > 10 * 1024 * 1024) { // 10MB
            throw new IOException("内容过大");
        }

        FileUtil.writeUtf8String(content, filePath);
    }

    /**
     * 获取文件扩展名
     */
    public static String getExtension(String fileName) {
        return FileUtil.extName(fileName);
    }

    /**
     * 获取文件名（不含扩展名）
     */
    public static String getMainName(String fileName) {
        return FileUtil.mainName(fileName);
    }

    /**
     * 获取文件大小
     */
    public static long getSize(String filePath) {
        return FileUtil.size(new File(filePath));
    }

    /**
     * 格式化文件大小
     */
    public static String formatSize(long size) {
        return FileUtil.readableFileSize(size);
    }
}
```

### 加密工具类
```java
// DigestUtil使用规范
public class CryptoUtils {

    /**
     * MD5加密
     */
    public static String md5(String data) {
        return DigestUtil.md5Hex(data);
    }

    /**
     * SHA256加密
     */
    public static String sha256(String data) {
        return DigestUtil.sha256Hex(data);
    }

    /**
     * SHA1加密
     */
    public static String sha1(String data) {
        return DigestUtil.sha1Hex(data);
    }

    /**
     * 生成文件MD5
     */
    public static String fileMd5(String filePath) throws IOException {
        return DigestUtil.md5Hex(new File(filePath));
    }

    /**
     * HMAC-SHA256签名
     */
    public static String hmacSha256(String data, String key) {
        return DigestUtil.hmacSha256Hex(data, key);
    }

    /**
     * 生成随机盐值
     */
    public static String generateSalt() {
        return RandomUtil.randomString(16);
    }

    /**
     * 密码加密（盐值+MD5）
     */
    public static String encryptPassword(String password, String salt) {
        return DigestUtil.md5Hex(password + salt);
    }

    /**
     * 验证密码
     */
    public static boolean verifyPassword(String password, String salt, String encryptedPassword) {
        return Objects.equals(encryptPassword(password, salt), encryptedPassword);
    }
}
```

## ⚠️ HutoL使用注意事项

### 禁止使用的HutoL功能
```java
// ❌ 禁止使用示例

// 1. 禁止使用DateUtil（使用LocalDateTime替代）
public class WrongUsage {
    public void wrongDateUsage() {
        // ❌ 错误：使用过时的Date
        Date now = DateUtil.date();
        String dateStr = DateUtil.formatDateTime(now);

        // ✅ 正确：使用LocalDateTime
        LocalDateTime now2 = DateTimeUtils.now();
        String dateStr2 = DateTimeUtils.format(now2, "yyyy-MM-dd HH:mm:ss");
    }
}

// 2. 禁止直接使用BeanUtil.copyProperties
public class WrongBeanUsage {
    public void wrongCopyUsage() {
        // ❌ 错误：直接使用BeanUtil
        UserVO vo = new UserVO();
        BeanUtil.copyProperties(userEntity, vo);

        // ✅ 正确：使用SmartBeanUtil
        UserVO vo2 = SmartBeanUtil.copy(userEntity, UserVO.class);
    }
}

// 3. 禁止使用不安全的文件操作
public class WrongFileUsage {
    public void wrongFileOperation() {
        // ❌ 错误：不安全的文件操作
        String content = FileUtil.readUtf8String("../../../etc/passwd");
        FileUtil.writeUtf8String(content, "/root/.bashrc");

        // ✅ 正确：使用安全的文件操作
        try {
            String content2 = FileUtils.readUtf8String("/safe/uploads/file.txt");
            FileUtils.writeUtf8String(content2, "/safe/uploads/file_copy.txt");
        } catch (IOException e) {
            log.error("文件操作失败", e);
        }
    }
}
```

### 性能优化建议
```markdown
✅ 重复使用的正则表达式要预编译
✅ 大量字符串拼接使用StringBuilder
✅ 集合操作预分配容量
✅ 避免在循环中创建对象
✅ 使用对象池管理重复对象
❌ 避免频繁创建正则表达式对象
❌ 避免在循环中进行JSON序列化
❌ 避免大量字符串拼接使用+
❌ 避免频繁的集合扩容操作
```

### 安全注意事项
```markdown
✅ 所有文件操作都要进行路径校验
✅ 所有加密操作都要使用安全的算法
✅ 所有网络操作都要有超时控制
✅ 所有输入都要进行参数验证
✅ 所有敏感信息都要脱敏处理
❌ 禁止直接使用用户输入作为文件路径
❌ 禁止使用不安全的加密算法
❌ 禁止明文传输敏感信息
❌ 禁止在前端进行安全校验
```

---

**🎯 核心原则**：
1. **安全第一** - 优先选择安全的工具类使用方式
2. **性能优先** - 注意工具类的性能影响
3. **一致性** - 项目内统一使用SmartBeanUtil、SmartJsonUtil等增强版工具
4. **可维护性** - 工具类使用要有统一规范
5. **可扩展性** - 基于HutoL的增强工具要易于扩展

**📖 相关文档**：
- [架构规范](./架构规范.md) - 工具类架构设计
- [编码规范](./编码规范.md) - 工具类编码规范
- [安全规范](./安全规范.md) - 工具类安全使用规范