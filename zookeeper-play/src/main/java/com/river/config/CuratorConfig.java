package com.river.config;


import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "zookeeper")
public class CuratorConfig {

    private final static int SESSION_TIMEOUT = 30 * 1000;

    private final static int CONNECTION_TIMEOUT = 3 * 1000;

    private String servers;


    @Bean
    public CuratorFramework client() {
        return CuratorFrameworkFactory.newClient(this.servers, SESSION_TIMEOUT, CONNECTION_TIMEOUT, new ExponentialBackoffRetry(1000, 3));
    }


    public void setServers(String servers) {
        this.servers = servers;
    }
}
