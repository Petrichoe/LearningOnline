package com.xuecheng.ucenter.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuecheng.ucenter.mapper.XcMenuMapper;
import com.xuecheng.ucenter.mapper.XcUserMapper;
import com.xuecheng.ucenter.model.dto.AuthParamsDto;
import com.xuecheng.ucenter.model.dto.XcUserExt;
import com.xuecheng.ucenter.model.po.XcMenu;
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

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class UserServiceImpl implements UserDetailsService {
    @Autowired
    XcUserMapper xcUserMapper;

    @Autowired
    ApplicationContext applicationContext;

    @Autowired
    XcMenuMapper menuMapper;

    /**
     * 获取用户信息
     * @param s
     * @return
     * @throws UsernameNotFoundException
     */
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
        /*// 用于最终返回给 Spring Security 的密码（数据库中存储的加密密码）
        String passwordDb = xcUser.getPassword();

        // 在将用户信息序列化为JSON存入令牌之前，先将密码清除，确保令牌安全
        xcUser.setPassword(null);
        String userjson = JSON.toJSONString(xcUser);

        String[] authorities = {"test"};

        // 使用从数据库中获取的、未被修改的密码来构建 UserDetails 对象
        UserDetails userDetails = User.withUsername(userjson).password(passwordDb).authorities(authorities).build();

        return userDetails;*/

        // 从数据库查询用户权限
        List<XcMenu> xcMenus = menuMapper.selectPermissionByUserId(xcUser.getId());
        List<String> permissions = new ArrayList<>();
        if (xcMenus.isEmpty()) {
            // 如果用户没有任何权限，给一个默认的空权限，防止后续代码出错
            permissions.add("none");
        } else {
            xcMenus.forEach(menu -> {
                // 将查询到的权限码（code）添加到list中
                permissions.add(menu.getCode());
            });
        }

        // 将权限列表转换为数组
        String[] authorities = permissions.toArray(new String[0]);

        // 保存数据库中的原始密码
        String passwordDb = xcUser.getPassword();
        // 为了安全，将密码设置为空，再转为JSON放入令牌
        xcUser.setPassword(null);
        // 将xcUser转为json串
        String userJson = JSON.toJSONString(xcUser);

        // 创建UserDetails对象
        UserDetails userDetails = User.withUsername(userJson)
                .password(passwordDb)
                .authorities(authorities) // 传入从数据库查询到的真实权限
                .build();
        return userDetails;

    }

}
