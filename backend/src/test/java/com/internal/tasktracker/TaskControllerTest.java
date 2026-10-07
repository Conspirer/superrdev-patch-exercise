package com.internal.tasktracker;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Locale;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Runs against the seeded H2 data (schema.sql + data.sql): 49 tasks, 2 of them archived.
@SpringBootTest
@AutoConfigureMockMvc
class TaskControllerTest {

    @Autowired
    private MockMvc mvc;

    @Test
    void searchExcludesArchivedTasks() throws Exception {
        // Archived tasks 20 and 21 mention "api" in their description
        mvc.perform(get("/api/tasks").param("q", "api").param("pageSize", "100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[*].archived", everyItem(is(false))))
                .andExpect(jsonPath("$.items[*].id", not(hasItems(20, 21))));
    }

    @Test
    void statusFilterAppliesWithoutSearchTerm() throws Exception {
        mvc.perform(get("/api/tasks").param("status", "DONE").param("pageSize", "100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(5))
                .andExpect(jsonPath("$.items[*].status", everyItem(is("DONE"))));
    }

    @Test
    void statusFilterAppliesToTitleMatches() throws Exception {
        // "Fix ..." titles exist in every status; only OPEN ones may come back
        mvc.perform(get("/api/tasks").param("q", "fix").param("status", "OPEN").param("pageSize", "100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[*].status", everyItem(is("OPEN"))));
    }

    @Test
    void paginatesInDatabaseWithCorrectTotal() throws Exception {
        mvc.perform(get("/api/tasks").param("page", "5").param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(47))
                .andExpect(jsonPath("$.items.length()").value(7));
    }

    @Test
    void invalidInputReturns400() throws Exception {
        mvc.perform(get("/api/tasks").param("status", "foo")).andExpect(status().isBadRequest());
        mvc.perform(get("/api/tasks").param("page", "0")).andExpect(status().isBadRequest());
        mvc.perform(get("/api/tasks").param("pageSize", "-5")).andExpect(status().isBadRequest());
        mvc.perform(get("/api/tasks").param("pageSize", "1000")).andExpect(status().isBadRequest());
    }

    @Test
    void lowercaseStatusWorksUnderTurkishLocale() throws Exception {
        // In tr-TR, "in_progress".toUpperCase() gives "İN_PROGRESS" (dotted capital I), which isn't an enum name
        Locale original = Locale.getDefault();
        Locale.setDefault(Locale.forLanguageTag("tr-TR"));
        try {
            mvc.perform(get("/api/tasks").param("status", "in_progress"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.items[*].status", everyItem(is("IN_PROGRESS"))));
        } finally {
            Locale.setDefault(original);
        }
    }
}
