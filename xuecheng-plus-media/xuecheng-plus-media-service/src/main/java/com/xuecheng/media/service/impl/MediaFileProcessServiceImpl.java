package com.xuecheng.media.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuecheng.media.mapper.MediaFilesMapper;
import com.xuecheng.media.mapper.MediaProcessHistoryMapper;
import com.xuecheng.media.mapper.MediaProcessMapper;
import com.xuecheng.media.model.po.MediaFiles;
import com.xuecheng.media.model.po.MediaProcess;
import com.xuecheng.media.model.po.MediaProcessHistory;
import com.xuecheng.media.service.MediaFileProcessService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
public class MediaFileProcessServiceImpl implements MediaFileProcessService {
    @Autowired
    private MediaProcessMapper mediaProcessMapper;

    @Autowired
    private MediaProcessHistoryMapper mediaProcessHistoryMapper;

    @Autowired
    private MediaFilesMapper mediaFilesMapper;

    @Override
    public List<MediaProcess> getMediaProcessList(int shardIndex, int shardTotal, int count) {
        List<MediaProcess> mediaProcessList = mediaProcessMapper.selectListByShardIndex(shardTotal, shardIndex, count);
        return mediaProcessList;
    }

    @Override
    public boolean startTask(long id) {
        int result = mediaProcessMapper.startTask(id);
        return result<=0?false:true;
    }
    @Transactional
    @Override
    public void saveProcessFinishStatus(Long taskId, String status, String fileId, String url, String errorMsg) {
        //先根据id找出这一整条数据判断是否存在
        MediaProcess mediaProcess = mediaProcessMapper.selectById(taskId);
        if (mediaProcess==null){
            return;
        }
        LambdaQueryWrapper<MediaProcess> lambdaQueryWrapper=new LambdaQueryWrapper<MediaProcess>().eq(MediaProcess::getId,taskId);
        //如果任务执行失败
        if (status.equals("3")){
            MediaProcess mediaProcess1 = new MediaProcess();
            mediaProcess1.setStatus("3");
            mediaProcess1.setFailCount(mediaProcess.getFailCount()+1);
            mediaProcess1.setErrormsg(errorMsg);
            mediaProcessMapper.update(mediaProcess1,lambdaQueryWrapper);
        }

        //如果任务执行成功，因为avi->mp4所以url要改
        MediaFiles mediaFiles = mediaFilesMapper.selectById(fileId);
        if (mediaFiles!=null){
            mediaFiles.setUrl(url);
            mediaFilesMapper.updateById(mediaFiles);
        }
        // 更新url,status
        mediaProcess.setUrl(url);
        mediaProcess.setStatus("2");
        mediaProcess.setFinishDate(LocalDateTime.now());
            //然后插入history表中再删除
        MediaProcessHistory mediaProcessHistory=new MediaProcessHistory();
        BeanUtils.copyProperties(mediaProcess,mediaProcessHistory);
        int insert = mediaProcessHistoryMapper.insert(mediaProcessHistory);
        if (insert>0){
            mediaProcessMapper.deleteById(mediaProcess.getId());
        }


    }
}
