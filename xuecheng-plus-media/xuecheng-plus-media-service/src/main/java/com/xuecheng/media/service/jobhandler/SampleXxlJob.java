package com.xuecheng.media.service.jobhandler;

import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;



@Slf4j
@Component
public class SampleXxlJob {
    private static Logger logger=LoggerFactory.getLogger(SampleXxlJob.class);

    @XxlJob("demoHandler")
    public void demoJobHandler() throws  Exception{
        System.out.println("处理视频....");
    }

    @XxlJob("demoHandler2")
    public void demoJobHandler2() throws  Exception{
        System.out.println("处理文档....");
    }

    /**
     * 2、分片广播任务
     */
    @XxlJob("shardingJobHandler")
    public void shardingJobHandler() throws Exception {

        // 分片参数
        int shardIndex = XxlJobHelper.getShardIndex();
        int shardTotal = XxlJobHelper.getShardTotal();

        log.info("分片参数：当前分片序号 = {}, 总分片数 = {}", shardIndex, shardTotal);
        log.info("开始执行第"+shardIndex+"批任务");

    }

}
