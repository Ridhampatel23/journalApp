package net.ridham.journalApp.repository;

import net.ridham.journalApp.entity.UserEntity;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface UserRepo extends MongoRepository<UserEntity, ObjectId> {
    UserEntity findByUserName(String userName);


    void deleteByUserName(String userName);
}

