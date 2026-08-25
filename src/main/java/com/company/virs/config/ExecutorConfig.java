package com.company.virs.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Configuration
public class ExecutorConfig {

    @Bean(
            destroyMethod = "shutdown"
    )
    public ExecutorService executorService(
            @Value("${virs.processing.thread-count:4}")
            int threadCount) {

        if (threadCount <= 0) {

            throw new IllegalArgumentException(
                    "Thread count must be greater than zero."
            );
        }

        return Executors.newFixedThreadPool(
                threadCount
        );
    }
}