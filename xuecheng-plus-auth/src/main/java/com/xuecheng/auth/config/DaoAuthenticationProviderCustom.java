package com.xuecheng.auth.config;

import com.alibaba.fastjson.JSON;
import com.xuecheng.ucenter.model.dto.AuthParamsDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

/**
 * @description 自定义DaoAuthenticationProvider
 * @author Mr.M
 * @date 2022/9/29 10:31
 * @version 1.0
 */
@Slf4j
@Component
public class DaoAuthenticationProviderCustom extends DaoAuthenticationProvider {


 //这里因为继承了 DaoAuthenticationProvider，要传给父类所以这里需要super.setUserDetailsService(userDetailsService);
 @Autowired
 public void setUserDetailsService(UserDetailsService userDetailsService) {
  super.setUserDetailsService(userDetailsService);
 }


 //如果认证类型是密码模式，则执行Spring Security原始的密码校验。否则跳过密码校验
 @Override
 //覆盖密码校验
 protected void additionalAuthenticationChecks(UserDetails userDetails, UsernamePasswordAuthenticationToken authentication) throws AuthenticationException {
  try {
   // 从 UserDetails 中获取我们之前存入的JSON字符串
   String principalJson = userDetails.getUsername();
   // 解析出 AuthParamsDto
   AuthParamsDto authParamsDto = JSON.parseObject(principalJson, AuthParamsDto.class);
   // 获取认证类型
   String authType = authParamsDto.getAuthType();

   // 如果是密码模式，就执行Spring Security原始的密码校验
   if ("password".equals(authType)) {
    super.additionalAuthenticationChecks(userDetails, authentication);
   }
   // 如果是其他模式（如wx），则不进行任何操作，即跳过密码校验

  } catch (Exception e) {
   log.error("自定义认证提供者解析认证类型异常", e);
   // 出现异常，也走原始的密码校验，确保安全
   super.additionalAuthenticationChecks(userDetails, authentication);
  }



 }

}