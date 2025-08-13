package com.xuecheng.ucenter.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuecheng.ucenter.mapper.XcUserMapper;
import com.xuecheng.ucenter.model.dto.AuthParamsDto;
import com.xuecheng.ucenter.model.dto.XcUserExt;
import com.xuecheng.ucenter.model.po.XcUser;
import com.xuecheng.ucenter.service.AuthService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class UserServiceImpl implements UserDetailsService {
    @Autowired
    XcUserMapper xcUserMapper;

    @Autowired
    ApplicationContext applicationContext;

    @Override
    public UserDetails loadUserByUsername(String s) throws UsernameNotFoundException {
        //统一传入数据
        AuthParamsDto authParamsDto= null;

        try {
            authParamsDto = JSON.parseObject(s, AuthParamsDto.class);
        }catch (Exception e){
            throw new RuntimeException("请求参数信息不符合要求");
        }

        String authType = authParamsDto.getAuthType();//认证类型

        //根据认证类型从指定的spring容器中取出指定的Bean
        String beanName = authType + "_authservice";
        AuthService authService = applicationContext.getBean(beanName, AuthService.class);
        XcUserExt xcUserExt = authService.execute(authParamsDto);

        UserDetails userPrincipal = getUserPrincipal(xcUserExt);

        return userPrincipal;

    }

    public UserDetails getUserPrincipal(XcUserExt xcUser){
        String[] authorities= {"test"};
        xcUser.setPassword(null);
        //将用户信息转为json
        String userjson = JSON.toJSONString(xcUser);
        UserDetails userDetails = User.withUsername(userjson).password(xcUser.getPassword()).authorities(authorities).build();
        return userDetails;
    }

}
