package com.river.configuration;

import com.dangdang.ddframe.job.config.JobCoreConfiguration;
import com.dangdang.ddframe.job.config.simple.SimpleJobConfiguration;
import com.dangdang.ddframe.job.lite.api.JobScheduler;
import com.dangdang.ddframe.job.lite.config.LiteJobConfiguration;
import com.dangdang.ddframe.job.reg.zookeeper.ZookeeperConfiguration;
import com.dangdang.ddframe.job.reg.zookeeper.ZookeeperRegistryCenter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.Assert;

import java.util.Map;

@Configuration
@ConfigurationProperties(prefix = "elastic-job")
public class ElasticConfig {

    private String servers;

    private String namespace;

    private Map<String,String> jobs;

    private ZookeeperRegistryCenter zookeeperRegistryCenter;
    /**
     * 连接zookeeper
     * @return
     */
    @Bean
    public ZookeeperRegistryCenter zookeeperRegistryCenter(){
        zookeeperRegistryCenter = new ZookeeperRegistryCenter(new ZookeeperConfiguration(servers, namespace));
        zookeeperRegistryCenter.init();
        return zookeeperRegistryCenter;
    }

    @Bean
    @ConditionalOnExpression("'${elastic-job}'.length() > 0")
    @ConditionalOnBean(ZookeeperRegistryCenter.class)
    public void registerJob(){
        Assert.notEmpty(jobs, String.format("job不能为空; jobs -> %s", jobs));

       jobs.entrySet().stream().forEach(job ->{

            String jobName = job.getKey().substring(job.getKey().lastIndexOf(".")+1);
            JobCoreConfiguration jobCoreConfiguration = JobCoreConfiguration
                    .newBuilder(jobName, job.getValue(), 1)
                    .build();
            LiteJobConfiguration liteJobConfiguration = LiteJobConfiguration
                    .newBuilder(new SimpleJobConfiguration(jobCoreConfiguration, job.getKey()))
                    .build();
            new JobScheduler(zookeeperRegistryCenter,liteJobConfiguration).init();
        });
    }

    public void setServers(String servers) {
        this.servers = servers;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public void setJobs(Map<String, String> jobs) {
        this.jobs = jobs;
    }
}
