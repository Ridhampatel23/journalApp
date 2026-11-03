package net.ridham.journalApp.scheduler;

import net.ridham.journalApp.entity.JournalEntry;
import net.ridham.journalApp.entity.UserEntity;
import net.ridham.journalApp.enums.Sentiment;
import net.ridham.journalApp.model.SentimentQueueMessage;
import net.ridham.journalApp.repository.UserRepoImpl;
import net.ridham.journalApp.service.SentimentService;
import net.ridham.journalApp.service.SqsProducerService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserSchedulerTest {

    @Mock
    private UserRepoImpl userRepo;

    @Mock
    private SentimentService sentimentService;

    @Mock
    private SqsProducerService sqsProducerService;

    @InjectMocks
    private UserScheduler userScheduler;

    @Test
    void sendsOneMessagePerUserWithTheMostFrequentMood() {
        UserEntity user = user("ridham@example.com",
                entry(Sentiment.HAPPY, 1), entry(Sentiment.SAD, 2), entry(Sentiment.HAPPY, 3));
        when(userRepo.getUsersForSentimentAnalysis()).thenReturn(Collections.singletonList(user));

        userScheduler.fetchUsersAndSendSentimentMail();

        // guards the old bug: the send used to sit inside the per-entry loop
        ArgumentCaptor<SentimentQueueMessage> message = ArgumentCaptor.forClass(SentimentQueueMessage.class);
        verify(sqsProducerService, times(1)).sendWeeklySentimentMessage(message.capture());
        assertThat(message.getValue().getEmail()).isEqualTo("ridham@example.com");
        assertThat(message.getValue().getSentiment()).endsWith("HAPPY");
    }

    @Test
    void ignoresEntriesOlderThanSevenDays() {
        UserEntity user = user("ridham@example.com",
                entry(Sentiment.SAD, 10), entry(Sentiment.SAD, 12), entry(Sentiment.SAD, 20),
                entry(Sentiment.HAPPY, 1));
        when(userRepo.getUsersForSentimentAnalysis()).thenReturn(Collections.singletonList(user));

        userScheduler.fetchUsersAndSendSentimentMail();

        ArgumentCaptor<SentimentQueueMessage> message = ArgumentCaptor.forClass(SentimentQueueMessage.class);
        verify(sqsProducerService).sendWeeklySentimentMessage(message.capture());
        assertThat(message.getValue().getSentiment()).endsWith("HAPPY");
    }

    @Test
    void classifiesPendingEntriesBeforeCounting() {
        JournalEntry pending = entry(null, 1);
        UserEntity user = user("ridham@example.com", pending);
        when(userRepo.getUsersForSentimentAnalysis()).thenReturn(Collections.singletonList(user));
        when(sentimentService.classifyAndStore(any(), any(), any())).thenReturn(Sentiment.ANXIOUS);

        userScheduler.fetchUsersAndSendSentimentMail();

        verify(sentimentService).classifyAndStore(pending.getId(), pending.getTitle(), pending.getContent());
        ArgumentCaptor<SentimentQueueMessage> message = ArgumentCaptor.forClass(SentimentQueueMessage.class);
        verify(sqsProducerService).sendWeeklySentimentMessage(message.capture());
        assertThat(message.getValue().getSentiment()).endsWith("ANXIOUS");
    }

    @Test
    void sendsNothingWhenThereAreNoEntriesThisWeek() {
        UserEntity user = user("ridham@example.com", entry(Sentiment.SAD, 30));
        when(userRepo.getUsersForSentimentAnalysis()).thenReturn(Collections.singletonList(user));

        userScheduler.fetchUsersAndSendSentimentMail();

        verify(sqsProducerService, never()).sendWeeklySentimentMessage(any());
        verifyNoInteractions(sentimentService);
    }

    private static UserEntity user(String email, JournalEntry... entries) {
        return UserEntity.builder()
                .userName("ridham")
                .password("hash")
                .email(email)
                .journalEntries(new ArrayList<>(Arrays.asList(entries)))
                .build();
    }

    private static JournalEntry entry(Sentiment sentiment, int daysAgo) {
        JournalEntry entry = new JournalEntry();
        entry.setTitle("title");
        entry.setContent("content");
        entry.setDate(LocalDateTime.now().minusDays(daysAgo));
        entry.setSentiment(sentiment);
        return entry;
    }
}
