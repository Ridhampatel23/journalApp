package net.ridham.journalApp.service;

import lombok.extern.slf4j.Slf4j;
import net.ridham.journalApp.entity.UserEntity;
import net.ridham.journalApp.repository.UserRepo;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/// A service class contains business logic which is then
/// called and used by controller class to make API endpoints
@Service
@Slf4j
public class UserService {

    @Autowired
    private UserRepo userRepository;

    private static final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public UserEntity saveNewUser(UserEntity user) {
        try {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
            user.setRoles(Arrays.asList("USER"));
            return userRepository.save(user);
        } catch (Exception e) {
            log.error("Exception", e);
            throw new RuntimeException("Error saving user", e);
        }
    }

    public UserEntity saveAdmin(String Id) {
        try {
            ObjectId objectId = new ObjectId(Id);
            Optional<UserEntity> optionalUser = userRepository.findById(objectId);

            if (!optionalUser.isPresent()){
                throw new  RuntimeException("User not found");
            }

            UserEntity user = optionalUser.get();

            user.setRoles(Arrays.asList("ADMIN", "USER"));

            return userRepository.save(user);

        } catch (Exception e) {
            log.error("Error while promoting user to admin", e);
            throw new RuntimeException("Error while promoting user to admin", e);
        }
    }

    public void saveEntryInUser(UserEntity user) {
        userRepository.save(user);
    }

    public List<UserEntity> getAllUsers() {
        return userRepository.findAll();
    }

    public Optional<UserEntity> findUserById(ObjectId id) {
        return userRepository.findById(id);
    }

    public void deleteUserById(ObjectId id) {
        userRepository.deleteById(id);
    }

    public UserEntity findUserByUsername(String userName) {
        return userRepository.findByUserName(userName);
    }
}
