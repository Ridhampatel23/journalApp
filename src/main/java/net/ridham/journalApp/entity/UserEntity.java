package net.ridham.journalApp.entity;

import lombok.Data;
import lombok.NonNull;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

/// These classes are called POJO classes : Plain Old Java Objects
@Document(collection = "users")
@Data
public class UserEntity {

        @Id
        private ObjectId id;
        @Indexed(unique = true) //Won't do it by default, you have to
        // add auto-index-creation in application.properties
        @NonNull
        private String userName;
        @NonNull
        private String password;
        //Declaring it as Arraylist will help you make it an exmpty list instead of null
        @DBRef
        private List<JournalEntry> journalEntries = new ArrayList<>();
        private List<String> roles;
}