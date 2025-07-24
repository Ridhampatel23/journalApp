package net.ridham.journalApp.service;

import lombok.extern.slf4j.Slf4j;
import net.ridham.journalApp.entity.JournalEntry;
import net.ridham.journalApp.entity.UserEntity;
import net.ridham.journalApp.repository.JournalEntryRepo;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/// A service class contains business logic which is then
/// called and used by controller class to make API endpoints
@Service
@Slf4j
public class JournalEntryService {


    @Autowired
    private JournalEntryRepo journalEntryRepo;

    @Autowired
    private UserService userService;

    @Transactional //Don't forget to enable transactional management in main file
    public void saveEntry (JournalEntry journalEntry, String userName) {
        try{
            UserEntity user = userService.findUserByUsername(userName);
            journalEntry.setDate(LocalDateTime.now());
            // Saving it at two different place and marking it a dbref will connect it
            JournalEntry saved = journalEntryRepo.save(journalEntry);
            // Keep in mind to save it as a transaction as , If the internet goes down midway
            // or something else happens, it might only save it in journal entries and not
            // in user!
            user.getJournalEntries().add(saved);
            userService.saveEntryInUser(user);
        } catch (Exception e) {
            log.error("Exception is: " , e );
            throw new RuntimeException("An error occured while saving journal entry", e);
        }
    }

    public void saveEntry (JournalEntry journalEntry) {
        try{
            journalEntryRepo.save(journalEntry);
        } catch (Exception e) {
            log.error("Exception" , e );
        }
    }



    public List<JournalEntry> getAllJournalEntries() {
        return journalEntryRepo.findAll();
    }

    public Optional<JournalEntry> findJournalEntryById(ObjectId id) {
        return journalEntryRepo.findById(id);
    }

    @Transactional
    public boolean deleteJournalEntryById(ObjectId id, String userName) {
        boolean removed = false;
        try {
        UserEntity user = userService.findUserByUsername(userName);
        removed = user.getJournalEntries().removeIf(x -> x.getId().equals(id));
        if (removed) {
            userService.saveEntryInUser(user); // If we use saveEntry on the same ID it updates the previous entry (mongoDB specific)
            journalEntryRepo.deleteById(id);
        }
    } catch (Exception e) {
            log.error("Exception" , e );
            throw new RuntimeException("An error occurred while deleting journal entry", e);
        }
        return removed;

    }



}
