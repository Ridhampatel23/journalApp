package net.ridham.journalApp.service;

import lombok.extern.slf4j.Slf4j;
import net.ridham.journalApp.ai.SentimentClassifier;
import net.ridham.journalApp.entity.JournalEntry;
import net.ridham.journalApp.enums.Sentiment;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class SentimentService {

    @Autowired
    private SentimentClassifier sentimentClassifier;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Async("sentimentExecutor")
    public void classifyAsync(ObjectId entryId, String title, String content) {
        classifyAndStore(entryId, title, content);
    }

    /// Returns null if classification fails; the entry stays pending for the next digest run
    public Sentiment classifyAndStore(ObjectId entryId, String title, String content) {
        try {
            Sentiment sentiment = sentimentClassifier.classify(title, content == null ? "" : content);
            // only fills a pending entry, so retries and duplicate calls are harmless
            Query query = Query.query(Criteria.where("_id").is(entryId).and("sentiment").is(null));
            mongoTemplate.updateFirst(query, Update.update("sentiment", sentiment), JournalEntry.class);
            return sentiment;
        } catch (Exception e) {
            log.warn("Sentiment classification failed for entry {}", entryId, e); // never log entry content
            return null;
        }
    }
}