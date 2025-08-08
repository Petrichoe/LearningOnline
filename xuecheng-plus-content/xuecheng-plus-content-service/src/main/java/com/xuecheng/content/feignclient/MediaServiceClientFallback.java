package com.xuecheng.content.feignclient;

import com.xuecheng.base.model.RestResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Component // 必须是一个Spring组件
public class MediaServiceClientFallback implements MediaServiceClient {

    @Override
    public String upload(@RequestPart("filedata") MultipartFile filedata, @RequestParam(value = "objectName",required = false)String objectName) {
        // 在这里编写降级处理逻辑
        log.debug("远程调用上传的文件接口发生熔断");
        
        // 返回一个通用的错误响应，告知上游服务调用失败
        // 使用您项目中的 RestResponse.validfail() 方法
        return null;
    }

    // 如果还有其他方法，也需要在这里提供一个降级实现
    // ...
}