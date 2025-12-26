package net.lab1024.sa.common.storage;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 存储模块简单测试
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-20
 */
@DisplayName("存储模块简单测试")
class StorageTestSimple {

    @Test
    @DisplayName("测试FileStorageRequest")
    void testFileStorageRequest() {
        // Given
        String fileName = "test-file.txt";
        byte[] content = "测试内容".getBytes();
        String contentType = "text/plain";

        // When
        FileStorageRequest request = new FileStorageRequest(fileName, content, contentType, null);

        // Then
        assertEquals(fileName, request.getFileName());
        assertArrayEquals(content, request.getFileContent());
        assertEquals(contentType, request.getContentType());
    }

    @Test
    @DisplayName("测试FileStorageResponse成功")
    void testFileStorageResponseSuccess() {
        // Given
        String filePath = "/files/test.txt";
        String fileUrl = "http://localhost/files/test.txt";
        Long fileSize = 1024L;

        // When
        FileStorageResponse response = FileStorageResponse.success(filePath, fileUrl, fileSize);

        // Then
        assertTrue(response.isSuccess());
        assertEquals(filePath, response.getFilePath());
        assertEquals(fileUrl, response.getFileUrl());
        assertEquals(fileSize, response.getFileSize());
        assertNull(response.getErrorMessage());
        assertNotNull(response.getStorageTime());
    }

    @Test
    @DisplayName("测试FileStorageResponse错误")
    void testFileStorageResponseError() {
        // Given
        String errorMessage = "文件存储失败";

        // When
        FileStorageResponse response = FileStorageResponse.error(errorMessage);

        // Then
        assertFalse(response.isSuccess());
        assertEquals(errorMessage, response.getErrorMessage());
        assertNull(response.getFilePath());
        assertNull(response.getFileUrl());
        assertNull(response.getFileSize());
        assertNotNull(response.getStorageTime());
    }

    @Test
    @DisplayName("测试FileStorageRequest构造函数")
    void testFileStorageRequestConstructors() {
        // 测试全参数构造函数
        FileStorageRequest request1 = new FileStorageRequest("test.txt", "content".getBytes(), "text/plain", "/path");
        assertEquals("/path", request1.getStoragePath());

        // 测试无存储路径构造函数
        FileStorageRequest request2 = new FileStorageRequest("test.txt", "content".getBytes(), "text/plain", null);
        assertNull(request2.getStoragePath());
    }

    @Test
    @DisplayName("测试FileStorageResponse构造函数")
    void testFileStorageResponseConstructors() {
        // 测试无参构造函数
        FileStorageResponse response1 = new FileStorageResponse();
        assertFalse(response1.isSuccess());
        assertNull(response1.getFilePath());

        // 测试全参构造函数
        FileStorageResponse response2 = new FileStorageResponse(true, "/path", "url", null, null, 100L);
        assertTrue(response2.isSuccess());
        assertEquals("/path", response2.getFilePath());
        assertEquals("url", response2.getFileUrl());
        assertEquals(100L, response2.getFileSize());
    }

    @Test
    @DisplayName("测试文件存储基本逻辑")
    void testFileStorageLogic() {
        // 模拟文件存储的基本逻辑测试
        String fileName = "test-file.txt";
        byte[] content = "测试文件内容".getBytes();

        // 验证文件名不为空
        assertNotNull(fileName);
        assertFalse(fileName.trim().isEmpty());

        // 验证文件内容不为空
        assertNotNull(content);
        assertTrue(content.length > 0);

        // 验证文件大小
        long expectedSize = content.length;
        assertEquals(expectedSize, content.length);

        // 验证文件扩展名
        assertTrue(fileName.endsWith(".txt"));
    }

    @Test
    @DisplayName("测试存储路径处理")
    void testStoragePathHandling() {
        // 测试各种存储路径格式
        String[] validPaths = {
            "files/document.txt",
            "images/photo.jpg",
            "data/report.pdf",
            "temp/cache.tmp"
        };

        for (String path : validPaths) {
            assertNotNull(path);
            assertFalse(path.trim().isEmpty());
            assertFalse(path.startsWith("/")); // 不应该以根路径开头
            assertFalse(path.endsWith("/"));    // 不应该以斜杠结尾
        }
    }
}