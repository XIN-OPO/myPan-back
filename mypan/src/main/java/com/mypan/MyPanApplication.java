package com.mypan;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@EnableAsync
@SpringBootApplication(scanBasePackages = {"com.mypan"})
@EnableTransactionManagement
@EnableScheduling
public class MyPanApplication {
    public static void main(String[] args) {
        SpringApplication.run(MyPanApplication.class,args);
    }
}
