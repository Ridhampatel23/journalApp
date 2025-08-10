package net.ridham.journalApp.repository;

import com.mongodb.assertions.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class UserReposirotyImplTest {

    @Autowired
    private UserRepoImpl userRepo;

    @Test
    public void test(){
        Assertions.assertNotNull(userRepo.getUsersForSentimentAnalysis());
    }

}
