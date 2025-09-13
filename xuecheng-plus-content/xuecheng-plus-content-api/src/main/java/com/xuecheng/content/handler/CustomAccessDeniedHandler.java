package com.xuecheng.content.handler;

import com.alibaba.fastjson.JSON;
import com.xuecheng.base.exception.RestErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class CustomAccessDeniedHandler implements AccessDeniedHandler {
    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException, ServletException {
        // 创建自定义的错误响应对象
        RestErrorResponse errorResponse = new RestErrorResponse("您没有操作此功能的权限");
        
        // 设置响应状态码为 403 (Forbidden)
        response.setStatus(HttpStatus.FORBIDDEN.value());
        // 设置响应内容类型为 JSON
        response.setContentType("application/json;charset=UTF-8");
        // 将错误响应对象转换为 JSON 字符串并写入响应体
        response.getWriter().write(JSON.toJSONString(errorResponse));
    }
}