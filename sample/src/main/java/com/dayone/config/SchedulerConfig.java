package com.dayone.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

@Configuration
public class SchedulerConfig implements SchedulingConfigurer {
    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        ThreadPoolTaskScheduler threadPool = new ThreadPoolTaskScheduler();

        int n = Runtime.getRuntime().availableProcessors(); // cpu 코어의 개수를 가져옴
        //코어 개수 만큼 쓰레드 생성.
        //CPU 작업 량이 많은 경우 코어 개수 + 1 정도
        //I/O 작업이 많은 경우 코어 개수 * 2 정도
        threadPool.setPoolSize(n);
        threadPool.initialize();

        taskRegistrar.setTaskScheduler(threadPool);
    }
}