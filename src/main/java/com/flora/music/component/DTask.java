package com.flora.music.component;

import com.flora.music.domain.Singer;
import com.flora.music.service.SingerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * @Author qinxiang
 * @Date 2023/3/5-下午5:46
 * 测试定时任务
 */
@Component
@EnableScheduling
@Slf4j
public class DTask {
    @Autowired
    private SingerService singerService;
    @Scheduled(cron = "0 0 18 * * ? ")
    public void dTask(){
        Singer singer = singerService.selectByPrimaryKey(11);
        log.info("定时任务执行成功：{}",singer);


    }
}
