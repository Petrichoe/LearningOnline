package com.xuecheng.content;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

/**
 * 这是一个专用于 Service 模块单元测试的启动类。
 * 它只在测试期间生效，用于加载测试所需的环境。
 */
@SpringBootApplication
// 关键：扫描 Feign 客户端，路径现在是 service 模块自己的包
@ComponentScan(basePackages = {"com.xuecheng.content", "com.xuecheng.messagesdk"})
public class ContentServiceTestApplication {
    // 这个类不需要 main 方法
}