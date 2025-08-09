package com.xuecheng;

import com.spring4all.swagger.EnableSwagger2Doc;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;

@SpringBootApplication
@EnableSwagger2Doc
@EnableFeignClients(basePackages = {"com.xuecheng.content.feignclient"})
@ComponentScan(basePackages = {"com.xuecheng.content", "com.xuecheng.messagesdk"},
        excludeFilters = {@ComponentScan.Filter(type = FilterType.ANNOTATION, classes = FeignClient.class)})
public class ContentApplication {
    public static void main(String[] args) {
        SpringApplication.run(ContentApplication.class,args);
    }
}



