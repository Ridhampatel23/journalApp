package net.ridham.journalApp.entity;

import lombok.*;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

/// These classes are called POJO classes : Plain Old Java Objects
@Document(collection = "users")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserEntity {

        @Id
        private ObjectId id;
        @Indexed(unique = true) //Won't do it by default, you have to
        // add auto-index-creation in application.properties
        @NonNull
        private String userName;
        private String email;
        private Boolean sentimentAnalysis;
        @NonNull
        private String password;

        private List<String> roles;
}