package net.ridham.journalApp.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import net.ridham.journalApp.cache.AppCache;
import net.ridham.journalApp.dto.response.UserResponseDTO;
import net.ridham.journalApp.entity.UserEntity;
import net.ridham.journalApp.mapper.UserMapper;
import net.ridham.journalApp.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/admin")
@Tag(name = "Admin", description = "Admin-only endpoints for user management and cache control")
@SecurityRequirement(name = "BearerAuth")
public class AdminController {

    @Autowired
    private UserService userService;

    @Autowired
    private AppCache appCache;

    @GetMapping("/all-users")
    @Operation(
            summary = "Get all users (admin)",
            description = "Returns a list of all users in the system. Intended for admin use."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Users found",
                    content = @Content(schema = @Schema(implementation = UserResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "No users found")
    })
    public ResponseEntity<List<UserResponseDTO>> getAllUsers() {
        List<UserEntity> allUsers = userService.getAllUsers();
        if (allUsers != null && !allUsers.isEmpty()) {
            List<UserResponseDTO> dtos = allUsers.stream()
                    .map(UserMapper::toDTO)
                    .collect(Collectors.toList());
            return new ResponseEntity<>(dtos, HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @PostMapping("/make-admin/{id}")
    @Operation(
            summary = "Promote user to admin",
            description = "Adds the Admin role to an existing user with the given id. Only accessible by existing admins."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User promoted to admin successfully",
                    content = @Content(schema = @Schema(implementation = UserResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<UserResponseDTO> makeAdmin(
            @Parameter(description = "ID of the user to promote to admin")
            @PathVariable String id
    ) {
        try {
            UserEntity updated = userService.saveAdmin(id);
            UserResponseDTO dto = UserMapper.toDTO(updated);
            return new ResponseEntity<>(dto, HttpStatus.OK);
        } catch (RuntimeException e) {
            if (e.getMessage() != null && e.getMessage().startsWith("User not found")) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @GetMapping("clear-app-cache")
    @Operation(
            summary = "Clear application cache",
            description = "Reinitializes the application cache. Intended for admin use."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cache cleared successfully")
    })
    public ResponseEntity<Void> clearAppCache() {
        appCache.init();
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
