package net.ridham.journalApp.service;

import net.ridham.journalApp.repository.UserRepo;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
public class UserServiceTests {

    @Autowired
    private UserRepo userRepository;

    @Test
    public void testFindByUsername() {
        assertNotNull(userRepository.findByUserName("Ridham"));
    }


}
