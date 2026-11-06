package net.ridham.journalApp.scheduler;

import net.ridham.journalApp.cache.AppCache;
import net.ridham.journalApp.entity.JournalEntry;
import net.ridham.journalApp.entity.UserEntity;
import net.ridham.journalApp.enums.Sentiment;
import net.ridham.journalApp.model.SentimentQueueMessage;
import net.ridham.journalApp.repository.JournalEntryRepo;
import net.ridham.journalApp.repository.UserRepoImpl;
import net.ridham.journalApp.service.SentimentService;
import net.ridham.journalApp.service.SqsProducerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class UserScheduler {

    @Autowired
    private SentimentService sentimentService;

    @Autowired
    private AppCache appCache;

    @Autowired
    private JournalEntryRepo journalEntryRepo;

    @Autowired
    private UserRepoImpl userRepo;

    @Autowired
    private SqsProducerService sqsProducerService;

    // To make scheduled tasks work, you need to communicate with springboot that you
    //have a @scheduled annotation. You can achieve it by annotating the main class
    // with @EnableScheduling
    //@Scheduled(cron = "0 0 8 * * SUN")
    public void fetchUsersAndSendSentimentMail() {

        LocalDateTime since = LocalDateTime.now().minusDays(7);

        List<UserEntity> users = userRepo.getUsersForSentimentAnalysis();

        for (UserEntity user : users) {

            List<JournalEntry> entries =
                    journalEntryRepo.findByUserIdAndDateAfter(
                            user.getId(),
                            since
                    );

            Map<Sentiment, Integer> counts =
                    new EnumMap<>(Sentiment.class);

            for (JournalEntry entry : entries) {

                Sentiment sentiment = entry.getSentiment();

                if (sentiment == null) {
                    sentiment = sentimentService.classifyAndStore(
                            entry.getId(),
                            entry.getTitle(),
                            entry.getContent()
                    );
                }

                if (sentiment != null) {
                    counts.merge(sentiment, 1, Integer::sum);
                }
            }

            Sentiment mostFrequent = counts.entrySet()
                    .stream()
                    .max(Map.Entry.comparingByValue())
                    .map(Map.Entry::getKey)
                    .orElse(null);

            if (mostFrequent != null) {

                SentimentQueueMessage message =
                        SentimentQueueMessage.builder()
                                .email(user.getEmail())
                                .sentiment(
                                        "Sentiment for the last 7 days is "
                                                + mostFrequent
                                )
                                .build();

                sqsProducerService.sendWeeklySentimentMessage(message);
            }
        }
    }
    //@Scheduled(cron = "0 */5 * * * *")
    public void clearAppCache(){
        appCache.init();
    }
}
