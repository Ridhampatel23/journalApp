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
import net.ridham.journalApp.dto.request.JournalEntryUpdateRequest;
import net.ridham.journalApp.dto.response.JournalEntryResponseDTO;
import net.ridham.journalApp.entity.JournalEntry;
import net.ridham.journalApp.entity.UserEntity;
import net.ridham.journalApp.mapper.JournalEntryMapper;
import net.ridham.journalApp.service.JournalEntryService;
import net.ridham.journalApp.service.SentimentService;
import net.ridham.journalApp.service.UserService;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import java.util.Objects;

import javax.validation.Valid;
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
    private SentimentService sentimentService;

    @Autowired
    private JournalEntryService journalEntryService;

    @Autowired
    private UserService userService;


    @Operation(
            summary = "Get all journal entries for current user",
            description = "Returns all journal entries belonging to the currently authenticated user."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Entries found",
                    content = @Content(schema = @Schema(implementation = JournalEntryResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "No entries found")
    })
    @GetMapping
    public ResponseEntity<Page<JournalEntryResponseDTO>> getAllJournalEntriesOfUser(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        if (page < 0 || size < 1 || size > 100) {
            return ResponseEntity.badRequest().build();
        }

        PageRequest pageable = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Direction.DESC, "date")
        );

        Page<JournalEntryResponseDTO> entries = journalEntryService
                .getEntries(authentication.getName(), pageable)
                .map(JournalEntryMapper::toDTO);

        return ResponseEntity.ok(entries);
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
            @Valid @RequestBody JournalEntryRequest request,
            Authentication authentication) {

        JournalEntry entry = JournalEntryMapper.fromRequest(request);

        JournalEntry saved = journalEntryService.saveEntry(entry, authentication.getName());

        sentimentService.classifyAsync(entry.getId(), entry.getTitle(), entry.getContent());

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(JournalEntryMapper.toDTO(saved));
    }


    @GetMapping("/id/{myID}")
    @Operation(summary = "Get a journal entry by ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Entry found"),
            @ApiResponse(responseCode = "400", description = "Invalid ID"),
            @ApiResponse(responseCode = "404", description = "Entry not found")
    })
    public ResponseEntity<JournalEntryResponseDTO> getJournalEntryById(
            @PathVariable String myID,
            Authentication authentication) {

        if (!ObjectId.isValid(myID)) {
            return ResponseEntity.badRequest().build();
        }

        return journalEntryService.findJournalEntryById(
                        new ObjectId(myID),
                        authentication.getName()
                )
                .map(entry -> ResponseEntity.ok(
                        JournalEntryMapper.toDTO(entry)
                ))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }


    @DeleteMapping("/id/{myID}")
    public ResponseEntity<Void> deleteJournalEntry(
            @PathVariable String myID,
            Authentication authentication) {

        if (!ObjectId.isValid(myID)) {
            return ResponseEntity.badRequest().build();
        }

        boolean deleted = journalEntryService.deleteJournalEntryById(
                new ObjectId(myID),
                authentication.getName()
        );

        return deleted
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
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
            @PathVariable String myID,
            @Valid @RequestBody JournalEntryUpdateRequest request,
            Authentication authentication) {

        if (!ObjectId.isValid(myID)) {
            return ResponseEntity.badRequest().build();
        }

        Optional<JournalEntry> found = journalEntryService
                .findJournalEntryById(
                        new ObjectId(myID),
                        authentication.getName()
                );

        if (found.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        JournalEntry entry = found.get();

        String oldTitle = entry.getTitle();
        String oldContent = entry.getContent();

        JournalEntryMapper.applyUpdate(request, entry);

        boolean changed =
                !Objects.equals(oldTitle, entry.getTitle()) ||
                        !Objects.equals(oldContent, entry.getContent());

        if (changed) {
            entry.setSentiment(null);
        }

        JournalEntry saved = changed
                ? journalEntryService.saveEntry(entry)
                : entry;

        if (changed) {
            sentimentService.classifyAsync(
                    saved.getId(),
                    saved.getTitle(),
                    saved.getContent()
            );
        }

        return ResponseEntity.ok(JournalEntryMapper.toDTO(saved));
    }
}
