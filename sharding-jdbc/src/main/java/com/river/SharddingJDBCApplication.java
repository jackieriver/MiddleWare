package com.river;


import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.annotation.Resource;
import javax.sql.DataSource;

@SpringBootApplication
@EnableTransactionManagement(proxyTargetClass = true)
@MapperScan({"com.river.mapper"})
public class SharddingJDBCApplication implements CommandLineRunner {
    @Resource
    private DataSource dataSource;

    public static void main(String[] args) {
        SpringApplication.run(SharddingJDBCApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println(dataSource);
    }
}
