package net.ridham.journalApp.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import net.ridham.journalApp.api.response.WeatherResponse;
import net.ridham.journalApp.dto.request.UserUpdateRequest;
import net.ridham.journalApp.repository.UserRepo;
import net.ridham.journalApp.service.UserService;
import net.ridham.journalApp.service.WeatherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

// A controller class makes end points and calls service class,
// Service class contains business logic

@RestController
@RequestMapping("/user")
@Tag(name = "User", description = "Endpoints for user management and user-specific actions")
@SecurityRequirement(name = "BearerAuth")
public class UserController {

    @Autowired
    private UserService userService;
    @Autowired
    private UserRepo userRepo;
    @Autowired
    private WeatherService weatherService;

    @PutMapping
    @Operation(
            summary = "Update current user",
            description = "Updates the current authenticated user's username and/or password."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "User updated successfully"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<Void> updateUser(
            @RequestBody UserUpdateRequest userUpdateRequest
    ) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userName = authentication.getName();

        userService.updateUser(userName, userUpdateRequest);

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @DeleteMapping
    @Operation(
            summary = "Delete current user",
            description = "Deletes the currently authenticated user from the system."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "User deleted successfully")
    })
    public ResponseEntity<Void> deleteUserById(
            Authentication authentication) {

        userService.deleteAccount(authentication.getName());

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/greeting")
    @Operation(
            summary = "Greeting with weather info",
            description = "Returns a greeting message for the current user, including basic weather information."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Greeting returned successfully",
                    content = @Content(schema = @Schema(implementation = String.class)))
    })
    public ResponseEntity<String> greeting() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        WeatherResponse weather = weatherService.getWeather("Mumbai");
        String greeting = "";
        if (weather != null) {
            greeting = " Weather feels like " + weather.getCurrent().getFeelslike();
        }
        return new ResponseEntity<>("Hi " + authentication.getName() + greeting, HttpStatus.OK);
    }
}
