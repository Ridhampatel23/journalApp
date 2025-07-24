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
@Slf4j
@Service
public class UserService {

    @Autowired
    private UserRepo userRepository;

    private static final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public void saveNewUser(UserEntity user) {
        try{
            user.setPassword(passwordEncoder.encode(user.getPassword()));
            user.setRoles(Arrays.asList("User"));
            userRepository.save(user);
        } catch (Exception e) {
            log.error("Exception" , e );
        }
    }

    public void saveAdmin(UserEntity user) {
        try{
            user.setPassword(passwordEncoder.encode(user.getPassword()));
            user.setRoles(Arrays.asList("User", "Admin"));
            userRepository.save(user);
        } catch (Exception e) {
            log.error("Exception" , e );
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
