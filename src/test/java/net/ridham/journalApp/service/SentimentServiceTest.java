package net.ridham.journalApp.service;

import net.ridham.journalApp.ai.SentimentClassifier;
import net.ridham.journalApp.entity.JournalEntry;
import net.ridham.journalApp.enums.Sentiment;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.UpdateDefinition;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SentimentServiceTest {

    @Mock
    private SentimentClassifier sentimentClassifier;

    @Mock
    private MongoTemplate mongoTemplate;

    @InjectMocks
    private SentimentService sentimentService;

    @Test
    void storesTheSentimentOnlyIfTheEntryIsStillPending() {
        ObjectId id = new ObjectId();
        when(sentimentClassifier.classify("Rough day", "Missed the bus")).thenReturn(Sentiment.SAD);

        Sentiment result = sentimentService.classifyAndStore(id, "Rough day", "Missed the bus");

        assertThat(result).isEqualTo(Sentiment.SAD);
        ArgumentCaptor<Query> query = ArgumentCaptor.forClass(Query.class);
        ArgumentCaptor<UpdateDefinition> update = ArgumentCaptor.forClass(UpdateDefinition.class);
        verify(mongoTemplate).updateFirst(query.capture(), update.capture(), eq(JournalEntry.class));

        // the "still null" condition is what makes retries and duplicate calls harmless
        Document filter = query.getValue().getQueryObject();
        assertThat(filter.get("_id")).isEqualTo(id);
        assertThat(filter).containsKey("sentiment");
        assertThat(filter.get("sentiment")).isNull();

        Document set = (Document) update.getValue().getUpdateObject().get("$set");
        assertThat(set.get("sentiment")).isEqualTo(Sentiment.SAD);
    }

    @Test
    void leavesTheEntryPendingWhenTheModelFails() {
        when(sentimentClassifier.classify(any(), any())).thenThrow(new RuntimeException("LLM down"));

        Sentiment result = sentimentService.classifyAndStore(new ObjectId(), "Title", "Content");

        assertThat(result).isNull();
        verifyNoInteractions(mongoTemplate);
    }

    @Test
    void sendsEmptyTextWhenContentIsNull() {
        when(sentimentClassifier.classify("Title", "")).thenReturn(Sentiment.NEUTRAL);

        Sentiment result = sentimentService.classifyAndStore(new ObjectId(), "Title", null);

        assertThat(result).isEqualTo(Sentiment.NEUTRAL);
        verify(sentimentClassifier).classify("Title", "");
    }
}
