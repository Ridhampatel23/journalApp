package net.ridham.journalApp.controller;

import net.ridham.journalApp.entity.UserEntity;
import net.ridham.journalApp.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/public")
public class PublicController {

    public PublicController() {
        System.out.println("PublicController loaded...");
    }

    @Autowired
    private UserService userService;



    @GetMapping("/health-check")
    String healthCheck() {
        return "OK";
    }

    @PostMapping("/create-user")
    public void createUser(@RequestBody UserEntity user){
        userService.saveNewUser(user);
    }
}
