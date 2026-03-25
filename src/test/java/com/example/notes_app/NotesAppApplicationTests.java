package com.example.notes_app;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
@Disabled("This test requires database connection - use integration tests instead")
class NotesAppApplicationTests {

    @Test
    void contextLoads() {
    }

}
