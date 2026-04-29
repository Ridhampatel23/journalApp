package net.ridham.journalApp.controller;

import net.ridham.journalApp.scheduler.UserScheduler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal")
public class InternalJobController {

    @Value("${internal.job.key}")
    private String internalJobKey;

    @Autowired
    private UserScheduler userScheduler;

    @PostMapping("/run-weekly-sentiment")
    public ResponseEntity<String> runWeeklySentiment(
            @RequestHeader(value = "X-Internal-Job-Key", required = false) String providedKey) {

        if (providedKey == null || !providedKey.equals(internalJobKey)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        }

        userScheduler.fetchUsersAndSendSentimentMail();
        return ResponseEntity.ok("Weekly sentiment producer triggered");
    }
}