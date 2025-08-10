package net.ridham.journalApp.scheduler;

import net.ridham.journalApp.cache.AppCache;
import net.ridham.journalApp.entity.JournalEntry;
import net.ridham.journalApp.entity.UserEntity;
import net.ridham.journalApp.enums.Sentiment;
import net.ridham.journalApp.repository.UserRepoImpl;
import net.ridham.journalApp.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
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
    private AppCache appCache;

    @Autowired
    private EmailService emailService;

    @Autowired
    private UserRepoImpl userRepo;

    // To make scheduled tasks work, you need to communicate with springboot that you
    //have a @scheduled annotation. You can achieve it by annotating the main class
    // with @EnableScheduling
    @Scheduled(cron = "0 0 8 * * SUN")
    public void fetchUsersAndSendSentimentMail(){
        List<UserEntity> users = userRepo.getUsersForSentimentAnalysis();
        for (UserEntity user : users) {
            List<JournalEntry> journalEntries = user.getJournalEntries();
            List<Sentiment> sentiments = journalEntries.stream().filter(x -> x.getDate().isAfter(LocalDateTime.now().minus(7, ChronoUnit.DAYS))).map(x -> x.getSentiment()).collect(Collectors.toList());
           Map<Sentiment, Integer> sentimentCounts = new HashMap<>();
           for (Sentiment sentiment : sentiments) {
               if (sentiment != null) {
                   sentimentCounts.put(sentiment, sentimentCounts.getOrDefault(sentiment, 0) + 1);
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
                   emailService.sendEmail(user.getEmail(), "Sentiment for last week", mostFrequentSentiment.toString());
               }
           }
        }

    }

    @Scheduled(cron = "0 */5 * * * *")
    public void clearAppCache(){
        appCache.init();
    }
}
