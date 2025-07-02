package net.ridham.journalApp.controller;

import net.ridham.journalApp.entity.UserEntity;
import net.ridham.journalApp.repository.UserRepo;
import net.ridham.journalApp.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// A controller class makes end points and calls service class,
// Service class contains business logic

@RestController
@RequestMapping("/user")
public class UserController {
    // Methods inside a controller class should always be public so they can
    // be accessed by Spring framework or External HTTP Requests!!
    @Autowired
    private UserService userService;
    @Autowired
    private UserRepo userRepo;

    @GetMapping
    public List<UserEntity> getAllUsers(){
       return userService.getAllUsers();
    }

    @PutMapping()
    public ResponseEntity<?> updateUser(@RequestBody UserEntity user){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userName = authentication.getName();
        UserEntity userInDB = userService.findUserByUsername(userName);
        userInDB.setUserName(user.getUserName());
        userInDB.setPassword(user.getPassword());
        userService.saveNewUser(userInDB);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);

    }

    @DeleteMapping
    public ResponseEntity<?> deleteUserById(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        userRepo.deleteByUserName(authentication.getName());
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }



}
