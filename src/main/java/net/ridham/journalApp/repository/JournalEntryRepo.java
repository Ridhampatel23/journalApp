package net.ridham.journalApp.repository;

import net.ridham.journalApp.entity.JournalEntry;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface JournalEntryRepo
        extends MongoRepository<JournalEntry, ObjectId> {

    Page<JournalEntry> findByUserId(
            ObjectId userId,
            Pageable pageable
    );

    Optional<JournalEntry> findByUserIdAndId(
            ObjectId userId,
            ObjectId id
    );

    List<JournalEntry> findByUserIdAndDateAfter(
            ObjectId userId,
            LocalDateTime date
    );

    long deleteByUserIdAndId(
            ObjectId userId,
            ObjectId id
    );

    long deleteByUserId(ObjectId userId);
}
