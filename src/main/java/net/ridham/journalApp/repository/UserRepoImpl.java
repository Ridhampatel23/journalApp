package net.ridham.journalApp.repository;

import net.ridham.journalApp.entity.UserEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class UserRepoImpl {

    @Autowired
    private MongoTemplate mongoTemplate;

    public List<UserEntity> getUsersForSentimentAnalysis() {
        Query query = new Query();

        query.addCriteria(
                new Criteria().andOperator(
                        Criteria.where("email").ne(null),
                        Criteria.where("email").ne(""),
                        Criteria.where("email").regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$"),
                        Criteria.where("sentimentAnalysis").is(true)
                )
        );

        return mongoTemplate.find(query, UserEntity.class);
    }

}
