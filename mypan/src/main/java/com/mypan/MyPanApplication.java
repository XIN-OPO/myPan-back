package com.mypan;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@EnableAsync
@SpringBootApplication(scanBasePackages = {"com.mypan"})
@MapperScan(basePackages = {"com.mypan.mappers"})
@EnableTransactionManagement
@EnableScheduling
public class MyPanApplication {
    public static void main(String[] args) {
        SpringApplication.run(MyPanApplication.class,args);
    }
}
