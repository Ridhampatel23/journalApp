package net.ridham.journalApp.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.Parameter;
import net.ridham.journalApp.dto.request.JournalEntryRequest;
import net.ridham.journalApp.dto.response.JournalEntryResponseDTO;
import net.ridham.journalApp.entity.JournalEntry;
import net.ridham.journalApp.entity.UserEntity;
import net.ridham.journalApp.mapper.JournalEntryMapper;
import net.ridham.journalApp.service.JournalEntryService;
import net.ridham.journalApp.service.UserService;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

// A controller class makes end points and calls service class,
// Service class contains business logic

@RestController
@RequestMapping("/journal")
@Tag(name = "Journal", description = "Endpoints for journal entries linked to the authenticated user")
@SecurityRequirement(name = "BearerAuth")
public class JournalEntryController {

    @Autowired
    private JournalEntryService journalEntryService;

    @Autowired
    private UserService userService;

    @GetMapping
    @Operation(
            summary = "Get all journal entries for current user",
            description = "Returns all journal entries belonging to the currently authenticated user."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Entries found",
                    content = @Content(schema = @Schema(implementation = JournalEntryResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "No entries found")
    })
    public ResponseEntity<List<JournalEntryResponseDTO>> getAllJournalEntriesOfUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String userName = auth.getName();
        UserEntity user = userService.findUserByUsername(userName);

        if (user == null || user.getJournalEntries() == null || user.getJournalEntries().isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        List<JournalEntryResponseDTO> dtos = user.getJournalEntries().stream()
                .map(JournalEntryMapper::toDTO)
                .collect(Collectors.toList());

        return new ResponseEntity<>(dtos, HttpStatus.OK);
    }

    @PostMapping
    @Operation(
            summary = "Create a new journal entry",
            description = "Creates a new journal entry for the currently authenticated user."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Entry created successfully",
                    content = @Content(schema = @Schema(implementation = JournalEntryResponseDTO.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<JournalEntryResponseDTO> createJournalEntry(
            @RequestBody JournalEntryRequest myEntryRequest
    ) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String userName = auth.getName();

            JournalEntry entry = JournalEntryMapper.fromRequest(myEntryRequest);
            journalEntryService.saveEntry(entry, userName);

            return new ResponseEntity<>(JournalEntryMapper.toDTO(entry), HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("id/{myID}")
    @Operation(
            summary = "Get a journal entry by ID",
            description = "Returns a single journal entry by ID, only if it belongs to the current user."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Entry found",
                    content = @Content(schema = @Schema(implementation = JournalEntryResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Entry not found")
    })
    public ResponseEntity<JournalEntryResponseDTO> getJournalEntryById(
            @Parameter(description = "ID of the journal entry") @PathVariable ObjectId myID
    ) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String userName = auth.getName();
        UserEntity user = userService.findUserByUsername(userName);

        List<JournalEntry> foundEntry = user.getJournalEntries().stream()
                .filter(journalEntry -> journalEntry.getId().equals(myID))
                .collect(Collectors.toList());

        if (!foundEntry.isEmpty()) {
            Optional<JournalEntry> journalEntry = journalEntryService.findJournalEntryById(myID);
            if (journalEntry.isPresent()) {
                return new ResponseEntity<>(JournalEntryMapper.toDTO(journalEntry.get()), HttpStatus.OK);
            }
        }

        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @DeleteMapping("id/{myID}")
    @Operation(
            summary = "Delete a journal entry by ID",
            description = "Deletes a single journal entry by ID, only if it belongs to the current user."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Entry deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Entry not found")
    })
    public ResponseEntity<Void> deleteJournalEntry(
            @Parameter(description = "ID of the journal entry to delete") @PathVariable ObjectId myID
    ) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String userName = auth.getName();
        boolean deleted = journalEntryService.deleteJournalEntryById(myID, userName);

        if (deleted) return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        else return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @PutMapping("id/{myID}")
    @Operation(
            summary = "Update a journal entry by ID",
            description = "Updates title and/or content of an existing journal entry, only if it belongs to the current user."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Entry updated successfully",
                    content = @Content(schema = @Schema(implementation = JournalEntryResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Entry not found")
    })
    public ResponseEntity<JournalEntryResponseDTO> updateJournalEntryById(
            @Parameter(description = "ID of the journal entry to update") @PathVariable ObjectId myID,
            @RequestBody JournalEntryRequest newEntryRequest
    ) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String userName = auth.getName();
        UserEntity user = userService.findUserByUsername(userName);

        List<JournalEntry> foundEntry = user.getJournalEntries().stream()
                .filter(journalEntry -> journalEntry.getId().equals(myID))
                .collect(Collectors.toList());

        if (!foundEntry.isEmpty()) {
            Optional<JournalEntry> journalEntry = journalEntryService.findJournalEntryById(myID);
            if (journalEntry.isPresent()) {
                JournalEntry old = journalEntry.get();
                JournalEntryMapper.applyUpdate(newEntryRequest, old);
                journalEntryService.saveEntry(old);
                return new ResponseEntity<>(JournalEntryMapper.toDTO(old), HttpStatus.OK);
            }
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
}
