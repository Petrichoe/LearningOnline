package com.xuecheng.media.service.jobhandler;

import com.xuecheng.base.utils.Mp4VideoUtil;
import com.xuecheng.media.model.po.MediaProcess;
import com.xuecheng.media.service.MediaFileProcessService;
import com.xuecheng.media.service.MediaFileService;
import com.xuecheng.media.service.impl.MediaFileProcessServiceImpl;
import com.xuecheng.media.service.impl.MediaFileServiceImpl;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * 任务处理类
 */
@Slf4j
@Component
public class VideoTask {
    @Autowired
    MediaFileProcessService mediaFileProcessService;

    @Autowired
    MediaFileService mediaFileService;

    @Value("${videoprocess.ffmpegpath}")
    private String ffmpegpath;

    @XxlJob("videoJobHandler")
    public void videoJobHandler() throws Exception {


        // 分片参数
        int shardIndex = XxlJobHelper.getShardIndex();//执行器序号
        int shardTotal = XxlJobHelper.getShardTotal();//执行器总数

        //确定cpu核心数
        int processors = Runtime.getRuntime().availableProcessors();


        //查询待处理任务
        List<MediaProcess> mediaProcessList = mediaFileProcessService.getMediaProcessList(shardIndex, shardTotal, processors);
        int size = mediaProcessList.size();//任务数量
        log.debug("取到视频处理任务数："+size);
        if (size<=0){
            return;
        }
        //创建一个线程池
        ExecutorService executorService = Executors.newFixedThreadPool(size);
        //使用的计数器
        CountDownLatch countDownLatch=new CountDownLatch(size);
        mediaProcessList.forEach(mediaProcess -> {
            executorService.execute(()->{ //每拿到一个任务就开始执行这个代码逻辑
                try {
                    //开启任务
                    long taskid = mediaProcess.getId();
                    boolean b = mediaFileProcessService.startTask(taskid);
                    if (!b) {
                        log.debug("抢占任务失败，任务id:{}", taskid);
                        return;
                    }
                    //下载minio视频到本地
                    String bucket = mediaProcess.getBucket();
                    String objectname = mediaProcess.getFilePath();
                    String fileId = mediaProcess.getFileId();
                    File file = mediaFileService.downloadFileFromMinIO(bucket, objectname);

                    if (file == null) {
                        log.debug("下载视频出错，任务id:{},bucketName:{},objectName:{}", taskid, bucket, objectname);
                        //保存处理失败的结果
                        mediaFileProcessService.saveProcessFinishStatus(taskid, "3", fileId, null, "下载视频到本地失败");
                        return;
                    }
                    //执行视频转码为MP4
                    String video_path = file.getAbsolutePath();

                    String MP4name = fileId + ".mp4";
                    File mp4File = null;
                    try {
                        mp4File = File.createTempFile("minio", ".mp4");
                    } catch (IOException e) {
                        log.debug("创建临时文件异常，{}", e.getMessage());
                        return;
                    }
                    String mp4_path = mp4File.getAbsolutePath();
                    //创建工具类对象以上为这个服务
                    Mp4VideoUtil mp4VideoUtil = new Mp4VideoUtil(ffmpegpath, video_path, MP4name, mp4_path);
                    String result = mp4VideoUtil.generateMp4();
                    if (!result.equals("success")) {
                        log.debug("视频转码原因，原因:{},bucket:{},objectName:{}", result, bucket, objectname);
                        mediaFileProcessService.saveProcessFinishStatus(taskid, "3", fileId, null, result);
                        return;
                    }

                    //上传到minio
                    boolean b1 = mediaFileService.addMediaFilesToMinIO(mp4File.getAbsolutePath(), "video/mp4", bucket, objectname);
                    //如果上传失败了

                    if (!b1) {
                        log.debug("上传MP4到minio失败，taskid:{}", taskid);
                        mediaFileProcessService.saveProcessFinishStatus(taskid, "3", fileId, null, "上传MP4到minio失败");
                        return;
                    }
                    String url = getFilePath(fileId, ".mp4");
                    //更新任务状态为成功
                    mediaFileProcessService.saveProcessFinishStatus(taskid, "2", fileId, url, "创建临时文件异常");
                }finally {
                    countDownLatch.countDown();
                }

            });
        });
        //指定一个最大限度的等待时间才不会一直等待
        countDownLatch.await(30, TimeUnit.MINUTES);

    }
    private String getFilePath(String fileMd5,String fileExt) {
        return fileMd5.substring(0, 1) + "/" + fileMd5.substring(1, 2) + "/" + fileMd5 + "/" + fileMd5+fileExt;
    }
}
