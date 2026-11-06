package net.ridham.journalApp.service;

import lombok.RequiredArgsConstructor;
import net.ridham.journalApp.entity.JournalEntry;
import net.ridham.journalApp.entity.UserEntity;
import net.ridham.journalApp.repository.JournalEntryRepo;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class JournalEntryService {

    private final JournalEntryRepo journalEntryRepo;
    private final UserService userService;

    private ObjectId getUserId(String username) {
        UserEntity user = userService.findUserByUsername(username);

        if (user == null) {
            throw new IllegalStateException(
                    "Authenticated user not found"
            );
        }

        return user.getId();
    }

    // CREATE
    public JournalEntry saveEntry(
            JournalEntry entry,
            String username) {

        entry.setUserId(getUserId(username));
        entry.setDate(LocalDateTime.now());

        return journalEntryRepo.save(entry);
    }

    // UPDATE (after ownership verification)
    public JournalEntry saveEntry(JournalEntry entry) {
        return journalEntryRepo.save(entry);
    }

    // READ ALL (paginated)
    public Page<JournalEntry> getEntries(
            String username,
            Pageable pageable) {

        return journalEntryRepo.findByUserId(
                getUserId(username),
                pageable
        );
    }

    // READ ONE (owner scoped)
    public Optional<JournalEntry> findJournalEntryById(
            ObjectId id,
            String username) {

        return journalEntryRepo.findByUserIdAndId(
                getUserId(username),
                id
        );
    }

    // DELETE ONE (owner scoped)
    public boolean deleteJournalEntryById(
            ObjectId id,
            String username) {

        return journalEntryRepo.deleteByUserIdAndId(
                getUserId(username),
                id
        ) > 0;
    }
}