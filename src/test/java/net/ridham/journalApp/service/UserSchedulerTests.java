package net.ridham.journalApp.service;

import net.ridham.journalApp.scheduler.UserScheduler;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class UserSchedulerTests {

    @Autowired
    private UserScheduler userScheduler;

    @Test
    public void testFetchUsersAndSendSentimentAnalysisEmail() {
        userScheduler.fetchUsersAndSendSentimentMail();
    }
}
