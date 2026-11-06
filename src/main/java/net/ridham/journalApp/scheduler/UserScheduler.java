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
    public void fetchUsersAndSendSentimentMail(){
        List<UserEntity> users = userRepo.getUsersForSentimentAnalysis();
        for (UserEntity user : users) {

            //We need the journal entries from the past 7 days to put out the weekly sentiment digest
            LocalDateTime since = LocalDateTime.now().minusDays(7);

            List<JournalEntry> journalEntries =
                    journalEntryRepo.findByUserIdAndDateAfter(
                            user.getId(),
                            since
                    );
            // anything the async pass missed (outage, dropped task) gets classified now, once
            for (JournalEntry entry : journalEntries) {
                if (entry.getSentiment() == null && entry.getDate().isAfter(LocalDateTime.now().minus(7, ChronoUnit.DAYS))) {
                    entry.setSentiment(sentimentService.classifyAndStore(entry.getId(), entry.getTitle(), entry.getContent()));
                }
            }
            List<Sentiment> sentiments = journalEntries.stream().filter(x -> x.getDate().isAfter(LocalDateTime.now().minus(7, ChronoUnit.DAYS))).map(x -> x.getSentiment()).collect(Collectors.toList());
           Map<Sentiment, Integer> sentimentCounts = new HashMap<>();
           for (Sentiment sentiment : sentiments) {
               if (sentiment != null) {
                   sentimentCounts.put(sentiment, sentimentCounts.getOrDefault(sentiment, 0) + 1);
               }
           }
               Sentiment mostFrequentSentiment = null;
               int maxCount = 0;
               for (Map.Entry<Sentiment, Integer> entry : sentimentCounts.entrySet()) {
                   if (entry.getValue() > maxCount) {
                       mostFrequentSentiment = entry.getKey();
                       maxCount = entry.getValue();
                   }
               }
               if (mostFrequentSentiment != null) {
                   SentimentQueueMessage sentimentQueueMessage = SentimentQueueMessage.builder().email(user.getEmail()).sentiment("Sentiment for the last 7 days is " + mostFrequentSentiment).build();
                   sqsProducerService.sendWeeklySentimentMessage(sentimentQueueMessage);
               }

        }

    }

    //@Scheduled(cron = "0 */5 * * * *")
    public void clearAppCache(){
        appCache.init();
    }
}
