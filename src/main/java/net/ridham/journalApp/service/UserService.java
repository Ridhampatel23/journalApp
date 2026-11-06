package net.ridham.journalApp.service;

import com.mongodb.DuplicateKeyException;
import lombok.extern.slf4j.Slf4j;
import net.ridham.journalApp.dto.request.UserUpdateRequest;
import net.ridham.journalApp.entity.UserEntity;
import net.ridham.journalApp.repository.JournalEntryRepo;
import net.ridham.journalApp.repository.UserRepo;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    @Autowired
    private JournalEntryRepo journalEntryRepo;

    private static final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public UserEntity saveNewUser(UserEntity user) {

        if (userRepository.findByUserName(user.getUserName()) != null) {
            throw new IllegalArgumentException("Username already exists");
        }
        try {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
            user.setRoles(Arrays.asList("USER"));
            return userRepository.save(user);
        } catch (DuplicateKeyException e) {
            log.error("Duplicate key exception", e);
            throw new RuntimeException(e);
        }

        catch (Exception e) {
            log.error("Exception", e);
            throw new RuntimeException("Error saving user", e);
        }
    }

    public UserEntity updateUser(
            String username,
            UserUpdateRequest request) {

        UserEntity user =
                userRepository.findByUserName(username);

        if (user == null) {
            throw new RuntimeException("User not found");
        }

        if (request.getUserName() != null
                && !request.getUserName().trim().isEmpty()){

            user.setUserName(request.getUserName());
        }

        if (request.getPassword() != null
                && !request.getPassword().trim().isEmpty()) {

            user.setPassword(
                    passwordEncoder.encode(request.getPassword())
            );
        }

        return userRepository.save(user);
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

    @Transactional
    public void deleteAccount(String username) {
        UserEntity user = userRepository.findByUserName(username);

        if (user == null) {
            throw new IllegalStateException("Authenticated user not found");
        }

        ObjectId userId = user.getId();

        journalEntryRepo.deleteByUserId(userId);
        userRepository.delete(user);

        log.info("Deleted user account and associated journals");
    }
    public UserEntity findUserByUsername(String userName) {
        return userRepository.findByUserName(userName);
    }
}
