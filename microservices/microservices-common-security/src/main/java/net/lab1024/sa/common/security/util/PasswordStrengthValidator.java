package net.lab1024.sa.common.security.util;

import lombok.extern.slf4j.Slf4j;

import java.util.regex.Pattern;

/**
 * 密码强度验证器
 * <p>
 * 功能：验证密码强度，确保用户密码符合安全策略
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-26
 */
@Slf4j
public class PasswordStrengthValidator {

    /**
     * 密码强度等级
     */
    public enum PasswordStrength {
        WEAK(0, "弱"),
        MEDIUM(1, "中等"),
        STRONG(2, "强"),
        VERY_STRONG(3, "非常强");

        private final int level;
        private final String description;

        PasswordStrength(int level, String description) {
            this.level = level;
            this.description = description;
        }

        public int getLevel() {
            return level;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * 密码策略配置
     */
    public static class PasswordPolicy {
        private int minLength = 8;
        private int maxLength = 128;
        private boolean requireUppercase = true;
        private boolean requireLowercase = true;
        private boolean requireDigit = true;
        private boolean requireSpecialChar = true;
        private int minStrengthLevel = 2; // 最小要求：中等强度

        public PasswordPolicy() {
        }

        public PasswordPolicy(int minLength, int maxLength, boolean requireUppercase,
                              boolean requireLowercase, boolean requireDigit,
                              boolean requireSpecialChar, int minStrengthLevel) {
            this.minLength = minLength;
            this.maxLength = maxLength;
            this.requireUppercase = requireUppercase;
            this.requireLowercase = requireLowercase;
            this.requireDigit = requireDigit;
            this.requireSpecialChar = requireSpecialChar;
            this.minStrengthLevel = minStrengthLevel;
        }

        // Getters
        public int getMinLength() { return minLength; }
        public int getMaxLength() { return maxLength; }
        public boolean isRequireUppercase() { return requireUppercase; }
        public boolean isRequireLowercase() { return requireLowercase; }
        public boolean isRequireDigit() { return requireDigit; }
        public boolean isRequireSpecialChar() { return requireSpecialChar; }
        public int getMinStrengthLevel() { return minStrengthLevel; }
    }

    /**
     * 默认密码策略
     */
    private static final PasswordPolicy DEFAULT_POLICY = new PasswordPolicy();

    // 正则表达式模式
    private static final Pattern UPPERCASE_PATTERN = Pattern.compile("[A-Z]");
    private static final Pattern LOWERCASE_PATTERN = Pattern.compile("[a-z]");
    private static final Pattern DIGIT_PATTERN = Pattern.compile("[0-9]");
    private static final Pattern SPECIAL_CHAR_PATTERN = Pattern.compile("[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?]");
    private static final Pattern CONTINUOUS_CHAR_PATTERN = Pattern.compile("(.)\\1{2,}"); // 连续3个相同字符
    private static final Pattern NUMERIC_SEQUENCE_PATTERN = Pattern.compile("(?:012|123|234|345|456|567|678|789|987|876|765|654|543|432|321|210)");

    /**
     * 验证密码强度
     *
     * @param password 待验证的密码
     * @return 密码强度等级
     */
    public static PasswordStrength validateStrength(String password) {
        if (password == null || password.isEmpty()) {
            return PasswordStrength.WEAK;
        }

        int score = 0;

        // 1. 长度评分（0-2分）
        int length = password.length();
        if (length >= 8) score += 1;
        if (length >= 12) score += 1;

        // 2. 字符类型评分（0-4分）
        boolean hasUppercase = UPPERCASE_PATTERN.matcher(password).find();
        boolean hasLowercase = LOWERCASE_PATTERN.matcher(password).find();
        boolean hasDigit = DIGIT_PATTERN.matcher(password).find();
        boolean hasSpecialChar = SPECIAL_CHAR_PATTERN.matcher(password).find();

        if (hasUppercase) score += 1;
        if (hasLowercase) score += 1;
        if (hasDigit) score += 1;
        if (hasSpecialChar) score += 1;

        // 3. 复杂度评分（0-2分）
        int typeCount = (hasUppercase ? 1 : 0) +
                        (hasLowercase ? 1 : 0) +
                        (hasDigit ? 1 : 0) +
                        (hasSpecialChar ? 1 : 0);
        if (typeCount >= 3) score += 1;
        if (typeCount == 4) score += 1;

        // 4. 扣分项（-2分）
        // 连续字符扣分
        if (CONTINUOUS_CHAR_PATTERN.matcher(password).find()) {
            score -= 1;
        }
        // 数字序列扣分
        if (NUMERIC_SEQUENCE_PATTERN.matcher(password).find()) {
            score -= 1;
        }

        // 转换为强度等级
        if (score <= 2) {
            return PasswordStrength.WEAK;
        } else if (score <= 4) {
            return PasswordStrength.MEDIUM;
        } else if (score <= 6) {
            return PasswordStrength.STRONG;
        } else {
            return PasswordStrength.VERY_STRONG;
        }
    }

    /**
     * 验证密码是否符合策略
     *
     * @param password 待验证的密码
     * @return 验证结果对象
     */
    public static ValidationResult validate(String password) {
        return validate(password, DEFAULT_POLICY);
    }

    /**
     * 验证密码是否符合指定策略
     *
     * @param password 待验证的密码
     * @param policy 密码策略
     * @return 验证结果对象
     */
    public static ValidationResult validate(String password, PasswordPolicy policy) {
        ValidationResult result = new ValidationResult();

        if (password == null || password.isEmpty()) {
            result.valid = false;
            result.addError("密码不能为空");
            return result;
        }

        // 1. 长度验证
        if (password.length() < policy.getMinLength()) {
            result.addError("密码长度不能少于" + policy.getMinLength() + "位");
        }
        if (password.length() > policy.getMaxLength()) {
            result.addError("密码长度不能超过" + policy.getMaxLength() + "位");
        }

        // 2. 大写字母验证
        if (policy.isRequireUppercase() && !UPPERCASE_PATTERN.matcher(password).find()) {
            result.addError("密码必须包含至少一个大写字母");
        }

        // 3. 小写字母验证
        if (policy.isRequireLowercase() && !LOWERCASE_PATTERN.matcher(password).find()) {
            result.addError("密码必须包含至少一个小写字母");
        }

        // 4. 数字验证
        if (policy.isRequireDigit() && !DIGIT_PATTERN.matcher(password).find()) {
            result.addError("密码必须包含至少一个数字");
        }

        // 5. 特殊字符验证
        if (policy.isRequireSpecialChar() && !SPECIAL_CHAR_PATTERN.matcher(password).find()) {
            result.addError("密码必须包含至少一个特殊字符（!@#$%^&*等）");
        }

        // 6. 连续字符验证
        if (CONTINUOUS_CHAR_PATTERN.matcher(password).find()) {
            result.addError("密码不能包含连续3个相同的字符");
        }

        // 7. 数字序列验证
        if (NUMERIC_SEQUENCE_PATTERN.matcher(password).find()) {
            result.addError("密码不能包含连续数字序列（如123、456）");
        }

        // 8. 强度等级验证
        PasswordStrength strength = validateStrength(password);
        if (strength.getLevel() < policy.getMinStrengthLevel()) {
            result.addError("密码强度不足，当前为" + strength.getDescription() +
                    "，要求至少为" + getStrengthDescription(policy.getMinStrengthLevel()));
        }

        result.valid = result.errors.isEmpty();
        result.strength = strength;

        return result;
    }

    /**
     * 验证结果对象
     */
    public static class ValidationResult {
        private boolean valid;
        private java.util.List<String> errors = new java.util.ArrayList<>();
        private PasswordStrength strength;

        public boolean isValid() {
            return valid;
        }

        public void setValid(boolean valid) {
            this.valid = valid;
        }

        public java.util.List<String> getErrors() {
            return errors;
        }

        public void addError(String error) {
            this.errors.add(error);
        }

        public PasswordStrength getStrength() {
            return strength;
        }

        public void setStrength(PasswordStrength strength) {
            this.strength = strength;
        }

        public String getErrorMessage() {
            return String.join("; ", errors);
        }
    }

    /**
     * 获取强度等级描述
     */
    private static String getStrengthDescription(int level) {
        switch (level) {
            case 0:
                return "弱";
            case 1:
                return "中等";
            case 2:
                return "强";
            case 3:
                return "非常强";
            default:
                return "未知";
        }
    }

    /**
     * 生成随机强密码
     *
     * @param length 密码长度
     * @return 随机强密码
     */
    public static String generateStrongPassword(int length) {
        if (length < 8) {
            throw new IllegalArgumentException("密码长度不能少于8位");
        }

        String upper = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String lower = "abcdefghijklmnopqrstuvwxyz";
        String digit = "0123456789";
        String special = "!@#$%^&*";
        String all = upper + lower + digit + special;

        java.security.SecureRandom random = new java.security.SecureRandom();
        StringBuilder password = new StringBuilder();

        // 确保包含各种字符类型
        password.append(upper.charAt(random.nextInt(upper.length())));
        password.append(lower.charAt(random.nextInt(lower.length())));
        password.append(digit.charAt(random.nextInt(digit.length())));
        password.append(special.charAt(random.nextInt(special.length())));

        // 填充剩余长度
        for (int i = 4; i < length; i++) {
            password.append(all.charAt(random.nextInt(all.length())));
        }

        // 打乱字符顺序
        return shuffleString(password.toString(), random);
    }

    /**
     * 打乱字符串顺序
     */
    private static String shuffleString(String input, java.security.SecureRandom random) {
        char[] characters = input.toCharArray();
        for (int i = 0; i < characters.length; i++) {
            int j = random.nextInt(characters.length);
            char temp = characters[i];
            characters[i] = characters[j];
            characters[j] = temp;
        }
        return new String(characters);
    }

    /**
     * 密码强度检查工具方法（供外部调用）
     */
    public static boolean isStrongPassword(String password) {
        return validate(password).isValid();
    }

    public static boolean isStrongPassword(String password, PasswordPolicy policy) {
        return validate(password, policy).isValid();
    }

    public static String getPasswordStrengthDescription(String password) {
        return validateStrength(password).getDescription();
    }
}
