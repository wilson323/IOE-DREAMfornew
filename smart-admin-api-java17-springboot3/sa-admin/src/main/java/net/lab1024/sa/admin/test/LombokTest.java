package net.lab1024.sa.admin.test;

import lombok.Data;

/**
 * Lombok Test Class - Verify @Data annotation generates getter/setter methods
 */
@Data
public class LombokTest {
    private String name;
    private Integer age;

    public static void main(String[] args) {
        LombokTest test = new LombokTest();
        test.setName("test");
        test.setAge(25);

        System.out.println("Name: " + test.getName());
        System.out.println("Age: " + test.getAge());
    }
}