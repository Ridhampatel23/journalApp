package net.ridham.journalApp.repository;

import net.ridham.journalApp.entity.UserEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataMongoTest
@Testcontainers(disabledWithoutDocker = true)
class UserRepoImplTest {

    // a throwaway MongoDB in Docker, started once for this class
    @Container
    static MongoDBContainer mongo = new MongoDBContainer("mongo:7.0");

    @DynamicPropertySource
    static void mongoProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongo::getReplicaSetUrl);
    }

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private UserRepoImpl userRepo;

    @BeforeEach
    void clean() {
        mongoTemplate.dropCollection(UserEntity.class);
    }

    @Test
    void findsOnlyOptedInUsersWithAValidEmail() {
        mongoTemplate.save(user("optedin", "a@example.com", true));
        mongoTemplate.save(user("optedout", "b@example.com", false));
        mongoTemplate.save(user("bademail", "not-an-email", true));
        mongoTemplate.save(user("noemail", null, true));

        List<UserEntity> found = userRepo.getUsersForSentimentAnalysis();

        assertThat(found).extracting(UserEntity::getUserName).containsExactly("optedin");
    }

    private static UserEntity user(String userName, String email, boolean optedIn) {
        return UserEntity.builder()
                .userName(userName)
                .password("hash")
                .email(email)
                .sentimentAnalysis(optedIn)
                .journalEntries(new ArrayList<>())
                .build();
    }
}
