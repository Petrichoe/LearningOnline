package com.xuecheng.content.service;

import com.xuecheng.content.model.dto.CoursePreviewDto;
import freemarker.template.TemplateException;

import java.io.File;
import java.io.IOException;

public interface CoursePublishService {
    /**
     * @description 获取课程预览信息
     * @param courseId 课程id
     * @return com.xuecheng.content.model.dto.CoursePreviewDto
     * @author Mr.M
     * @date 2022/9/16 15:36
     */
    public CoursePreviewDto getCoursePreviewInfo(Long courseId);

    /**
     * @description 提交审核
     * @param courseId  课程id
     * @return void
     * @author Mr.M
     * @date 2022/9/18 10:31
     */
    public void commitAudit(Long companyId,Long courseId);

    /**
     * 存储课程发布信息
     * @param courseId
     */
    public void savePublish(Long companyId,Long courseId);

    /**
     * 进行页面静态化
     * @param courseId
     * @return
     */
    public File generateCourseHtml(Long courseId);

    /**
     * 上传静态文件
     * @param courseId
     * @param file
     */
    public void uploadCourseHtml(Long courseId,File file);
}
