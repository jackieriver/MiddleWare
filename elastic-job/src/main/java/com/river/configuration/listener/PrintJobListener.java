package com.river.configuration.listener;

import com.dangdang.ddframe.job.executor.ShardingContexts;
import com.dangdang.ddframe.job.lite.api.listener.ElasticJobListener;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;


@Slf4j
@Component
public class PrintJobListener implements ElasticJobListener {

    @Override
    public void beforeJobExecuted(ShardingContexts shardingContexts) {
        log.info("任务开始执行...");
    }

    @Override
    public void afterJobExecuted(ShardingContexts shardingContexts) {
        log.info("任务执行完毕...");
    }
}
