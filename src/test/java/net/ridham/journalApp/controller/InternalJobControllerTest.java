package net.ridham.journalApp.controller;

import net.ridham.journalApp.scheduler.UserScheduler;
import net.ridham.journalApp.service.UserDetailsServiceImpl;
import net.ridham.journalApp.utils.JWTUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(InternalJobController.class)
@TestPropertySource(properties = "internal.job.key=test-key")
class InternalJobControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserScheduler userScheduler;

    // needed by the security setup (JWTFilter and SpringSecurity), not by this controller
    @MockBean
    private JWTUtil jwtUtil;

    @MockBean
    private UserDetailsServiceImpl userDetailsService;

    @Test
    void rejectsARequestWithoutTheKey() throws Exception {
        mockMvc.perform(post("/internal/run-weekly-sentiment"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(userScheduler);
    }

    @Test
    void rejectsAWrongKey() throws Exception {
        mockMvc.perform(post("/internal/run-weekly-sentiment").header("X-Internal-Job-Key", "guess"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(userScheduler);
    }

    @Test
    void runsTheDigestWithTheRightKey() throws Exception {
        mockMvc.perform(post("/internal/run-weekly-sentiment").header("X-Internal-Job-Key", "test-key"))
                .andExpect(status().isOk());

        verify(userScheduler).fetchUsersAndSendSentimentMail();
    }
}
