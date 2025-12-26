package com.example.exercise;

import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

@Service
public class Exercise2 {
    @Resource
    private SomeService someService;

    public void doSomething() {
        someService.process();
        // 已修复：使用@Resource注解进行依赖注入
    }
}

interface SomeService {
    void process();
}
