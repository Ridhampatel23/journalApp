package net.ridham.journalApp.controller;

import lombok.extern.slf4j.Slf4j;
import net.ridham.journalApp.dto.request.LoginRequest;
import net.ridham.journalApp.dto.request.UserRegistrationRequest;
import net.ridham.journalApp.dto.response.UserResponseDTO;
import net.ridham.journalApp.entity.UserEntity;
import net.ridham.journalApp.mapper.UserMapper;
import net.ridham.journalApp.model.SentimentQueueMessage;
import net.ridham.journalApp.scheduler.UserScheduler;
import net.ridham.journalApp.service.SqsProducerService;
import net.ridham.journalApp.service.UserDetailsServiceImpl;
import net.ridham.journalApp.service.UserService;
import net.ridham.journalApp.utils.JWTUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

@RestController
@RequestMapping("/public")
@Slf4j
@Tag(name = "Public", description = "Public endpoints for health-check, signup, and login")
public class PublicController {

    public PublicController() {
        System.out.println("PublicController loaded...");
    }

    @Autowired
    private JWTUtil jwtUtil;

    @Autowired
    private UserService userService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @GetMapping("/health-check")
    @Operation(
            summary = "Health check",
            description = "Simple endpoint to verify that the API is up and running."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "API is healthy")
    })
    String healthCheck() {
        return "OK";
    }

    @PostMapping("/signup")
    @Operation(
            summary = "User signup",
            description = "Registers a new user and returns basic user info (no password)."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "User created",
                    content = @Content(schema = @Schema(implementation = UserResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input")
    })
    public ResponseEntity<UserResponseDTO> createUser(
            @RequestBody UserRegistrationRequest request
    ) {
        UserEntity newUser = UserMapper.fromRegistration(request);
        UserEntity saved = userService.saveNewUser(newUser);
        UserResponseDTO dto = UserMapper.toDTO(saved);
        return new ResponseEntity<>(dto, HttpStatus.CREATED);
    }

    @PostMapping("/login")
    @Operation(
            summary = "User login",
            description = "Authenticates a user and returns a JWT token if credentials are valid."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Login successful, JWT returned",
                    content = @Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "400", description = "Invalid username/password")
    })
    public ResponseEntity<String> login(
            @RequestBody LoginRequest loginRequest
    ) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getUserName(),
                            loginRequest.getPassword()
                    )
            );
            UserDetails userDetails = userDetailsService.loadUserByUsername(loginRequest.getUserName());
            String jwt = jwtUtil.generateToken(userDetails.getUsername());
            return new ResponseEntity<>(jwt, HttpStatus.OK);
        } catch (Exception e) {
            log.error(e.getMessage());
            return new ResponseEntity<>("Incorrect username or password", HttpStatus.BAD_REQUEST);
        }
    }

    @Autowired
    private UserScheduler userScheduler;

    @Autowired
    private SqsProducerService sqsProducerService;

    @PostMapping("/test-sqs")
    public String testSqs() {
        SentimentQueueMessage sentimentQueueMessage = SentimentQueueMessage.builder().email("Ridhamsangani.23@gmail.com").sentiment("Sentiment for the last 7 days is " + "HAPPY").build();
        sqsProducerService.sendWeeklySentimentMessage(sentimentQueueMessage);
        return "Triggered SQS sentiment producer";
    }
}
