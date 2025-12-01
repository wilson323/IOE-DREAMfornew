package net.lab1024.sa.common.util;


import cn.hutool.core.util.StrUtil;

import java.util.*;

/**
 * 独有的字符串工具类
 *
 * @Author 1024创新实验室-主任: 卓大
 * @Date 2021-09-02 20:21:10
 * @Wechat zhuoda1024
 * @Email lab1024@163.com
 * @Copyright  <a href"https://1024lab.net">1024创新实验室</a>
 */
public class SmartStringUtil extends StrUtil {

    // ===============split =======================

    public static Set<String> splitConvertToSet(String str, String split) {
        if (isEmpty(str)) {
            return new HashSet<String>();
        }
        String[] splitArr = str.split(split);
        HashSet<String> set = new HashSet<String>(splitArr.length);
        Collections.addAll(set, splitArr);
        return set;
    }

    public static List<String> splitConvertToList(String str, String split) {
        if (isEmpty(str)) {
            return new ArrayList<String>();
        }
        String[] splitArr = str.split(split);
        ArrayList<String> list = new ArrayList<String>(splitArr.length);
        list.addAll(Arrays.asList(splitArr));
        return list;
    }

    // ===============split Integer=======================

    public static List<Integer> splitConvertToIntList(String str, String split, int defaultVal) {
        if (isEmpty(str)) {
            return new ArrayList<Integer>();
        }
        String[] strArr = str.split(split);
        List<Integer> list = new ArrayList<Integer>(strArr.length);
        for (int i = 0; i < strArr.length; i++) {
            try {
                int parseInt = Integer.parseInt(strArr[i]);
                list.add(parseInt);
            } catch (NumberFormatException e) {
                list.add(defaultVal);
                continue;
            }
        }
        return list;
    }

    public static Set<Integer> splitConvertToIntSet(String str, String split, int defaultVal) {
        if (isEmpty(str)) {
            return new HashSet<Integer>();
        }
        String[] strArr = str.split(split);
        HashSet<Integer> set = new HashSet<Integer>(strArr.length);
        for (int i = 0; i < strArr.length; i++) {
            try {
                int parseInt = Integer.parseInt(strArr[i]);
                set.add(parseInt);
            } catch (NumberFormatException e) {
                set.add(defaultVal);
                continue;
            }
        }
        return set;
    }

    public static Set<Integer> splitConvertToIntSet(String str, String split) {
        return splitConvertToIntSet(str, split, 0);
    }

    public static List<Integer> splitConvertToIntList(String str, String split) {
        return splitConvertToIntList(str, split, 0);
    }

    public static int[] splitConvertToIntArray(String str, String split, int defaultVal) {
        if (isEmpty(str)) {
            return new int[0];
        }
        String[] strArr = str.split(split);
        int[] result = new int[strArr.length];
        for (int i = 0; i < strArr.length; i++) {
            try {
                result[i] = Integer.parseInt(strArr[i]);
            } catch (NumberFormatException e) {
                result[i] = defaultVal;
                continue;
            }
        }
        return result;
    }

    public static int[] splitConvertToIntArray(String str, String split) {
        return splitConvertToIntArray(str, split, 0);
    }

    // ===============split 2 Long=======================

    public static List<Long> splitConvertToLongList(String str, String split, long defaultVal) {
        if (isEmpty(str)) {
            return new ArrayList<Long>();
        }
        String[] strArr = str.split(split);
        List<Long> list = new ArrayList<Long>(strArr.length);
        for (int i = 0; i < strArr.length; i++) {
            try {
                long parseLong = Long.parseLong(strArr[i]);
                list.add(parseLong);
            } catch (NumberFormatException e) {
                list.add(defaultVal);
                continue;
            }
        }
        return list;
    }

    public static List<Long> splitConvertToLongList(String str, String split) {
        return splitConvertToLongList(str, split, 0L);
    }

    public static long[] splitConvertToLongArray(String str, String split, long defaultVal) {
        if (isEmpty(str)) {
            return new long[0];
        }
        String[] strArr = str.split(split);
        long[] result = new long[strArr.length];
        for (int i = 0; i < strArr.length; i++) {
            try {
                result[i] = Long.parseLong(strArr[i]);
            } catch (NumberFormatException e) {
                result[i] = defaultVal;
                continue;
            }
        }
        return result;
    }

    public static long[] splitConvertToLongArray(String str, String split) {
        return splitConvertToLongArray(str, split, 0L);
    }

    // ===============split convert byte=======================

    public static List<Byte> splitConvertToByteList(String str, String split, byte defaultVal) {
        if (isEmpty(str)) {
            return new ArrayList<Byte>();
        }
        String[] strArr = str.split(split);
        List<Byte> list = new ArrayList<Byte>(strArr.length);
        for (int i = 0; i < strArr.length; i++) {
            try {
                byte parseByte = Byte.parseByte(strArr[i]);
                list.add(parseByte);
            } catch (NumberFormatException e) {
                list.add(defaultVal);
                continue;
            }
        }
        return list;
    }

    public static List<Byte> splitConvertToByteList(String str, String split) {
        return splitConvertToByteList(str, split, (byte) 0);
    }

    public static byte[] splitConvertToByteArray(String str, String split, byte defaultVal) {
        if (isEmpty(str)) {
            return new byte[0];
        }
        String[] strArr = str.split(split);
        byte[] result = new byte[strArr.length];
        for (int i = 0; i < strArr.length; i++) {
            try {
                result[i] = Byte.parseByte(strArr[i]);
            } catch (NumberFormatException e) {
                result[i] = defaultVal;
                continue;
            }
        }
        return result;
    }

    public static byte[] splitConvertToByteArray(String str, String split) {
        return splitConvertToByteArray(str, split, (byte) 0);
    }

    // ===============split convert double=======================

    public static List<Double> splitConvertToDoubleList(String str, String split, double defaultVal) {
        if (isEmpty(str)) {
            return new ArrayList<Double>();
        }
        String[] strArr = str.split(split);
        List<Double> list = new ArrayList<Double>(strArr.length);
        for (int i = 0; i < strArr.length; i++) {
            try {
                double parseByte = Double.parseDouble(strArr[i]);
                list.add(parseByte);
            } catch (NumberFormatException e) {
                list.add(defaultVal);
                continue;
            }
        }
        return list;
    }

    public static List<Double> splitConvertToDoubleList(String str, String split) {
        return splitConvertToDoubleList(str, split, 0);
    }

    public static double[] splitConvertToDoubleArray(String str, String split, double defaultVal) {
        if (isEmpty(str)) {
            return new double[0];
        }
        String[] strArr = str.split(split);
        double[] result = new double[strArr.length];
        for (int i = 0; i < strArr.length; i++) {
            try {
                result[i] = Double.parseDouble(strArr[i]);
            } catch (NumberFormatException e) {
                result[i] = defaultVal;
                continue;
            }
        }
        return result;
    }

    public static double[] splitConvertToDoubleArray(String str, String split) {
        return splitConvertToDoubleArray(str, split, 0);
    }

    // ===============split convert float=======================

    public static List<Float> splitConvertToFloatList(String str, String split, float defaultVal) {
        if (isEmpty(str)) {
            return new ArrayList<Float>();
        }
        String[] strArr = str.split(split);
        List<Float> list = new ArrayList<Float>(strArr.length);
        for (int i = 0; i < strArr.length; i++) {
            try {
                float parseByte = Float.parseFloat(strArr[i]);
                list.add(parseByte);
            } catch (NumberFormatException e) {
                list.add(defaultVal);
                continue;
            }
        }
        return list;
    }

    public static List<Float> splitConvertToFloatList(String str, String split) {
        return splitConvertToFloatList(str, split, 0f);
    }

    public static float[] splitConvertToFloatArray(String str, String split, float defaultVal) {
        if (isEmpty(str)) {
            return new float[0];
        }
        String[] strArr = str.split(split);
        float[] result = new float[strArr.length];
        for (int i = 0; i < strArr.length; i++) {
            try {
                result[i] = Float.parseFloat(strArr[i]);
            } catch (NumberFormatException e) {
                result[i] = defaultVal;
                continue;
            }
        }
        return result;
    }

    public static float[] splitConvertToFloatArray(String str, String split) {
        return splitConvertToFloatArray(str, split, 0f);
    }


    public static String upperCaseFirstChar(String str) {
        if (str != null && !str.isEmpty()) {
            char firstChar = str.charAt(0);
            if (Character.isUpperCase(firstChar)) {
                return str;
            } else {
                char[] values = str.toCharArray();
                values[0] = Character.toUpperCase(firstChar);
                return new String(values);
            }
        } else {
            return str;
        }
    }

    public static String replace(String content, int begin, int end, String newStr) {
        if (begin < content.length() && begin >= 0) {
            if (end <= content.length() && end >= 0) {
                if (begin > end) {
                    return content;
                } else {
                    StringBuilder starStr = new StringBuilder();

                    for (int i = begin; i < end; ++i) {
                        starStr.append(newStr);
                    }

                    return content.substring(0, begin) + starStr + content.substring(end);
                }
            } else {
                return content;
            }
        } else {
            return content;
        }
    }

    /**
     * 获取文件扩展名
     * @param filename 文件名
     * @return 扩展名，不包含点号；如果没有扩展名则返回空字符串
     */
    public static String getExtension(String filename) {
        if (isEmpty(filename)) {
            return "";
        }

        // 查找最后一个点号的位置
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex == -1 || lastDotIndex == filename.length() - 1) {
            return "";
        }

        // 查找最后一个斜杠的位置（处理路径中的文件名）
        int lastSlashIndex = Math.max(filename.lastIndexOf('/'), filename.lastIndexOf('\\'));
        if (lastDotIndex < lastSlashIndex) {
            return "";
        }

        return filename.substring(lastDotIndex + 1);
    }

    /**
     * 获取文件名（不包含扩展名）
     * @param filename 完整文件名
     * @return 不包含扩展名的文件名
     */
    public static String getFileNameWithoutExtension(String filename) {
        if (isEmpty(filename)) {
            return "";
        }

        // 查找最后一个点号的位置
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex == -1) {
            return filename;
        }

        // 查找最后一个斜杠的位置（处理路径中的文件名）
        int lastSlashIndex = Math.max(filename.lastIndexOf('/'), filename.lastIndexOf('\\'));
        if (lastDotIndex < lastSlashIndex) {
            return filename;
        }

        // 提取路径部分（如果有）
        String pathPart = "";
        if (lastSlashIndex != -1) {
            pathPart = filename.substring(0, lastSlashIndex + 1);
        }

        return pathPart + filename.substring(lastSlashIndex + 1, lastDotIndex);
    }

    /**
     * 获取文件扩展名（包含点号）
     * @param filename 文件名
     * @return 扩展名，包含点号；如果没有扩展名则返回空字符串
     */
    public static String getExtensionWithDot(String filename) {
        String extension = getExtension(filename);
        return isEmpty(extension) ? "" : "." + extension;
    }

    /**
     * 检查文件是否具有指定扩展名
     * @param filename 文件名
     * @param extension 扩展名（不包含点号，不区分大小写）
     * @return 如果文件具有指定扩展名则返回true
     */
    public static boolean hasExtension(String filename, String extension) {
        if (isEmpty(filename) || isEmpty(extension)) {
            return false;
        }
        return getExtension(filename).equalsIgnoreCase(extension);
    }

    /**
     * 检查是否为图片文件
     * @param filename 文件名
     * @return 如果是图片文件则返回true
     */
    public static boolean isImageFile(String filename) {
        if (isEmpty(filename)) {
            return false;
        }
        String extension = getExtension(filename);
        Set<String> imageExtensions = Set.of("jpg", "jpeg", "png", "gif", "bmp", "webp", "svg", "ico");
        return imageExtensions.contains(extension.toLowerCase());
    }

    /**
     * 检查是否为文档文件
     * @param filename 文件名
     * @return 如果是文档文件则返回true
     */
    public static boolean isDocumentFile(String filename) {
        if (isEmpty(filename)) {
            return false;
        }
        String extension = getExtension(filename);
        Set<String> docExtensions = Set.of("pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx", "txt", "rtf");
        return docExtensions.contains(extension.toLowerCase());
    }

    /**
     * 检查是否为压缩文件
     * @param filename 文件名
     * @return 如果是压缩文件则返回true
     */
    public static boolean isArchiveFile(String filename) {
        if (isEmpty(filename)) {
            return false;
        }
        String extension = getExtension(filename);
        Set<String> archiveExtensions = Set.of("zip", "rar", "7z", "tar", "gz", "bz2");
        return archiveExtensions.contains(extension.toLowerCase());
    }

    /**
     * 安全的字符串截取
     * @param str 原字符串
     * @param maxLength 最大长度
     * @return 截取后的字符串，如果原字符串长度超过最大长度则添加省略号
     */
    public static String truncate(String str, int maxLength) {
        if (isEmpty(str) || str.length() <= maxLength) {
            return str;
        }
        return str.substring(0, maxLength - 3) + "...";
    }

    /**
     * 隐藏字符串中间部分
     * @param str 原字符串
     * @param startLen 开始保留的字符数
     * @param endLen 结尾保留的字符数
     * @param replaceChar 替换字符
     * @return 隐藏中间部分的字符串
     */
    public static String hideMiddle(String str, int startLen, int endLen, String replaceChar) {
        if (isEmpty(str)) {
            return str;
        }

        int len = str.length();
        if (len <= startLen + endLen) {
            return str;
        }

        String start = str.substring(0, startLen);
        String end = str.substring(len - endLen);
        String middle = repeat(replaceChar != null ? replaceChar : "*", len - startLen - endLen);

        return start + middle + end;
    }

    /**
     * 重复字符串
     * @param str 要重复的字符串
     * @param count 重复次数
     * @return 重复后的字符串
     */
    public static String repeat(String str, int count) {
        if (isEmpty(str) || count <= 0) {
            return "";
        }

        StringBuilder result = new StringBuilder(str.length() * count);
        for (int i = 0; i < count; i++) {
            result.append(str);
        }
        return result.toString();
    }

    /**
     * 转换为驼峰命名
     * @param str 下划线或连字符分隔的字符串
     * @return 驼峰命名的字符串
     */
    public static String toCamelCase(String str) {
        if (isEmpty(str)) {
            return str;
        }

        StringBuilder result = new StringBuilder();
        boolean capitalizeNext = false;

        for (char c : str.toCharArray()) {
            if (c == '_' || c == '-') {
                capitalizeNext = true;
            } else {
                if (capitalizeNext) {
                    result.append(Character.toUpperCase(c));
                    capitalizeNext = false;
                } else {
                    result.append(Character.toLowerCase(c));
                }
            }
        }

        return result.toString();
    }

    /**
     * 转换为下划线命名
     * @param str 驼峰命名的字符串
     * @return 下划线命名的字符串
     */
    public static String toSnakeCase(String str) {
        if (isEmpty(str)) {
            return str;
        }

        StringBuilder result = new StringBuilder();
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if (Character.isUpperCase(c)) {
                if (i > 0) {
                    result.append('_');
                }
                result.append(Character.toLowerCase(c));
            } else {
                result.append(c);
            }
        }

        return result.toString();
    }
}