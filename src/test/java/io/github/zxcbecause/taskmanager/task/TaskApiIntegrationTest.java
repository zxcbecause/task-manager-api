package io.github.zxcbecause.taskmanager.task;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class TaskApiIntegrationTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper mapper;

    private long createTask(String json) throws Exception {
        String body = mvc.perform(post("/api/tasks").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        JsonNode node = mapper.readTree(body);
        return node.get("id").asLong();
    }

    @Test
    void createAndFetchTask() throws Exception {
        mvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title": "Learn Spring", "priority": "HIGH", "dueDate": "2030-01-01"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", containsString("/api/tasks/")))
                .andExpect(jsonPath("$.title").value("Learn Spring"))
                .andExpect(jsonPath("$.status").value("TODO"))
                .andExpect(jsonPath("$.priority").value("HIGH"))
                .andExpect(jsonPath("$.overdue").value(false));
    }

    @Test
    void listSupportsFiltersAndSearch() throws Exception {
        createTask("{\"title\": \"Buy milk\", \"priority\": \"LOW\"}");
        createTask("{\"title\": \"Fix login bug\", \"priority\": \"HIGH\"}");
        createTask("{\"title\": \"Fix CSS\", \"priority\": \"LOW\", \"status\": \"DONE\"}");

        mvc.perform(get("/api/tasks").param("search", "fix"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page.totalElements").value(2));

        mvc.perform(get("/api/tasks").param("priority", "LOW").param("status", "TODO"))
                .andExpect(jsonPath("$.page.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].title").value("Buy milk"));
    }

    @Test
    void updateAndChangeStatus() throws Exception {
        long id = createTask("{\"title\": \"Draft\"}");

        mvc.perform(put("/api/tasks/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\": \"Final\", \"description\": \"Ready\", \"priority\": \"HIGH\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Final"))
                .andExpect(jsonPath("$.priority").value("HIGH"));

        mvc.perform(patch("/api/tasks/{id}/status", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\": \"DONE\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("DONE"));

        mvc.perform(get("/api/tasks/stats"))
                .andExpect(jsonPath("$.done").value(1));
    }

    @Test
    void deleteThenGetReturns404() throws Exception {
        long id = createTask("{\"title\": \"Temporary\"}");

        mvc.perform(delete("/api/tasks/{id}", id)).andExpect(status().isNoContent());

        mvc.perform(get("/api/tasks/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Task not found"));
    }

    @Test
    void validationErrorsAreReported() throws Exception {
        mvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\": \"   \"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.title").value("title is required"));
    }

    @Test
    void invalidEnumInQueryReturns400() throws Exception {
        mvc.perform(get("/api/tasks").param("status", "SLEEPING"))
                .andExpect(status().isBadRequest());
    }
}
