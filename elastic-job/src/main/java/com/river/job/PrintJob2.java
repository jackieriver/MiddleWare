package com.river.job;

import com.dangdang.ddframe.job.api.ShardingContext;
import com.dangdang.ddframe.job.api.simple.SimpleJob;
import lombok.extern.slf4j.Slf4j;


@Slf4j
//@Component
//@ElasticSimpleJob(cron = "0/5 * * * * ?", jobName = "testJob")
public class PrintJob2 implements SimpleJob {
    @Override
    public void execute(ShardingContext shardingContext) {
        log.info("MyElasticJob - 2");


    }
}
