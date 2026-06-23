package com.medbid;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableKafka
@EnableCaching
@EnableAsync
@EnableRetry
@EnableScheduling
public class SmartMedTenderApplication {

    public static void main(String[] args) {
        SpringApplication.run(SmartMedTenderApplication.class, args);
    }
}
