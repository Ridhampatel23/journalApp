package net.ridham.journalApp.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.ThreadPoolExecutor;

@Configuration
@EnableAsync                          // turns on @Async; without it, @Async methods run on the caller's thread
public class AsyncConfig {

    @Bean("sentimentExecutor")        // named so @Async("sentimentExecutor") picks this pool
    public ThreadPoolTaskExecutor sentimentExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);          // 2 threads always run; at most 2 LLM calls at once normally
        executor.setMaxPoolSize(4);           // extra threads are added ONLY after the queue is full
        executor.setQueueCapacity(500);       // up to 500 entries wait their turn
        executor.setThreadNamePrefix("sentiment-");   // shows up in logs, easy to grep
        // if 500 are already waiting, drop the new task instead of throwing into the user's request;
        // the entry stays null and the digest classifies it later
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.DiscardPolicy());
        return executor;
    }
}