package com.lazy.todo;
import com.google.gson.Gson;
import com.lazy.todo.payload.request.ProjectRequest;
import com.lazy.todo.payload.request.TaskRequest;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import javax.transaction.Transactional;
import java.util.Date;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@DirtiesContext
public class SpringBootTodoProjectAcceptanceTests {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    Gson gson;

    ProjectRequest PROJECT_REQUEST_1 = new ProjectRequest("testTitle1", "testDescription1");
    ProjectRequest PROJECT_REQUEST_2 = new ProjectRequest("testTitle2", "testDescription2");

    TaskRequest TASK_REQUEST_1 = new TaskRequest("taskTitle1", "taskDescription1");


    @Value("${lazy.app.jwtSecret}")
    private String jwtSecret;

    @Value("${lazy.app.jwtExpirationMs}")
    private int jwtExpirationMs;

    public String generateJwtToken() {

        return Jwts.builder()
                .setSubject(("testUser1"))
                .setIssuedAt(new Date())
                .setExpiration(new Date((new Date()).getTime() + jwtExpirationMs))
                .signWith(SignatureAlgorithm.HS512, jwtSecret)
                .compact();
    }

    @Test
    @Transactional
    @WithMockUser(username = "testUser1")
    public void newTaskTest() throws Exception {
        mockMvc.perform(
                        post("/api/project/new")
                                .contentType(MediaType.APPLICATION_JSON)
                                .header("Authorization", "bearer:" + generateJwtToken())
                                .content(gson.toJson(PROJECT_REQUEST_1))
                                .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title", is(PROJECT_REQUEST_1.getTitle())))
                .andReturn().getResponse().getContentAsString();
    }

    @Test
    @WithMockUser(username = "testUser1")
    public void getProjectByIdTest() throws Exception {
        mockMvc.perform(
                        get("/api/project/1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .header("Authorization", "bearer:" + generateJwtToken())
                                .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title", is(PROJECT_REQUEST_1.getTitle())))
                .andReturn().getResponse().getContentAsString();
    }

    @Test
    @WithMockUser(username = "testUser1")
    public void getAllProjectsTest() throws Exception {
        mockMvc.perform(
                        get("/api/project/all")
                                .contentType(MediaType.APPLICATION_JSON)
                                .header("Authorization", "bearer:" + generateJwtToken())
                                .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.[0].title", is(PROJECT_REQUEST_1.getTitle())))
                .andReturn().getResponse().getContentAsString();
    }

    @Test
    @Transactional
    @WithMockUser(username = "testUser1")
    public void updateProjectByIdTest() throws Exception {
        //check that the project is the expected initial values
        mockMvc.perform(
                        get("/api/project/1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .header("Authorization", "bearer:" + generateJwtToken())
                                .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title", is(PROJECT_REQUEST_1.getTitle())))
                .andReturn().getResponse().getContentAsString();
        //change the values
        mockMvc.perform(
                        put("/api/project/1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .header("Authorization", "bearer:" + generateJwtToken())
                                .content(gson.toJson(PROJECT_REQUEST_2))
                                .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title", is(PROJECT_REQUEST_2.getTitle())))
                .andReturn().getResponse().getContentAsString();
        //check the values have changed properly
        mockMvc.perform(
                        get("/api/project/1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .header("Authorization", "bearer:" + generateJwtToken())
                                .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title", is(PROJECT_REQUEST_2.getTitle())))
                .andReturn().getResponse().getContentAsString();
    }
    @Test
    @Transactional
    @WithMockUser(username = "testUser1")
    public void deleteProjectByIdTest() throws Exception {
        //start with a delete request and check that the expected task is returned
        mockMvc.perform(
                        delete("/api/project/1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .header("Authorization", "bearer:" + generateJwtToken())
                                .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title", is(PROJECT_REQUEST_1.getTitle())))
                .andReturn().getResponse().getContentAsString();
        //request the deleted task to verify it no longer exists
        mockMvc.perform(
                        get("/api/project/1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .header("Authorization", "bearer:" + generateJwtToken())
                                .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isNoContent())
                .andReturn().getResponse().getContentAsString();
    }
    @Test
    @Transactional
    @WithMockUser(username = "testUser1")
    public void addTaskToProjectTest() throws Exception {
        mockMvc.perform(
                        put("/api/project/addTask/1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .header("Authorization", "bearer:" + generateJwtToken())
                                .content(gson.toJson(TASK_REQUEST_1))
                                .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tasks.[0].title", is(TASK_REQUEST_1.getTitle())))
                .andReturn().getResponse().getContentAsString();
        }
}
