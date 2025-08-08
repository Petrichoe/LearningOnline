package com.xuecheng.content.service.jobhandler;

import com.xuecheng.content.service.CoursePublishService;
import com.xuecheng.messagesdk.model.po.MqMessage;
import com.xuecheng.messagesdk.service.MessageProcessAbstract;
import com.xuecheng.messagesdk.service.MqMessageService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import freemarker.template.TemplateException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;

@Component
@Slf4j
public class CoursePublishTask extends MessageProcessAbstract {

    @Autowired
    CoursePublishService coursePublishService;

    //任务调度入口
    @XxlJob("CoursePublishJobHandler")
    public void coursePublishJobHandler() throws Exception {
        // 分片参数
        int shardIndex = XxlJobHelper.getShardIndex();
        int shardTotal = XxlJobHelper.getShardTotal();
        log.debug("shardIndex="+shardIndex+",shardTotal="+shardTotal);
        //参数:分片序号、分片总数、消息类型、一次最多取到的任务数量、一次任务调度执行的超时时间
        process(shardIndex,shardTotal,"course_publish",30,60);
    }

    /**
     * 执行任务课程发布任务的逻辑
     * @param mqMessage 执行任务内容
     * @return
     */
    @Override
    public boolean execute(MqMessage mqMessage) {
        String businessKey1 = mqMessage.getBusinessKey1();
        Long courseId = Long.valueOf(businessKey1);
        //课程静态化
        generateCourseHtml(mqMessage,courseId);
        //课程索引
        saveCourseIndex(mqMessage,courseId);
        //课程缓存
        saveCourseCache(mqMessage,courseId);
        return true;
    }


    private void saveCourseCache(MqMessage mqMessage, Long courseId) {
        //消息id
        Long taskid = mqMessage.getId();
        MqMessageService mqMessageService=this.getMqMessageService();

        //任务幂等性处理
        int stageThree = mqMessageService.getStageThree(taskid);
        if (stageThree>0){
            log.debug("课程静态化处理完成，无需处理...");
            return;
        }

        //开始存入索引

        //任务处理完成设置状态为已完成
        mqMessageService.completedStageThree(taskid);
    }

    private void saveCourseIndex(MqMessage mqMessage, Long courseId) {
        //消息id
        Long taskid = mqMessage.getId();
        MqMessageService mqMessageService=this.getMqMessageService();

        //任务幂等性处理
        int stageTwo = mqMessageService.getStageTwo(taskid);
        if (stageTwo>0){
            log.debug("课程索引已写入，无需处理...");
            return;
        }

        //开始存入索引

        //任务处理完成设置状态为已完成
        mqMessageService.completedStageTwo(taskid);

    }

    private void generateCourseHtml(MqMessage mqMessage, Long courseId) {
        //消息id
        Long taskid = mqMessage.getId();
        MqMessageService mqMessageService=this.getMqMessageService();

        //任务幂等性处理
        int stageOne = mqMessageService.getStageOne(taskid);
        if (stageOne>0){
            log.debug("课程静态化处理完成，无需处理...");
            return;
        }

        //开始进行课程静态化
        File file=coursePublishService.generateCourseHtml(courseId);

        //上传静态化页面
        coursePublishService.uploadCourseHtml(courseId,file);


        //任务处理完成设置状态为已完成
        mqMessageService.completedStageOne(taskid);


    }
}
