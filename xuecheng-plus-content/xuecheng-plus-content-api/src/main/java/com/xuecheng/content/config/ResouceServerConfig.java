package com.xuecheng.content.config;

import com.xuecheng.content.handler.CustomAccessDeniedHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.token.TokenStore;

/**
 * @description 资源服务配置
 * @author Mr.M
 * @date 2022/10/18 16:33
 * @version 1.0
 */
 @Configuration
 @EnableResourceServer
 @EnableGlobalMethodSecurity(securedEnabled = true,prePostEnabled = true)
 public class ResouceServerConfig extends ResourceServerConfigurerAdapter {


  //资源服务标识
  public static final String RESOURCE_ID = "xuecheng-plus";

  @Autowired
  TokenStore tokenStore;

  @Override
  public void configure(ResourceServerSecurityConfigurer resources) {
   resources.resourceId(RESOURCE_ID)//资源 id
           .tokenStore(tokenStore)
           .stateless(true);
  }

 @Override
 public void configure(HttpSecurity http) throws Exception {
  http.csrf().disable()
          .authorizeRequests()
          .anyRequest().permitAll()
          .and() // 使用 and() 连接配置
          .exceptionHandling() // 开始异常处理的配置
          .accessDeniedHandler(new CustomAccessDeniedHandler()); // <-- 核心修改：添加这一行来指定我们自定义的处理器

 }

 }
