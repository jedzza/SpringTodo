package com.lazy.todo;

import com.google.gson.Gson;
import com.lazy.todo.controllers.TaskController;
import com.lazy.todo.models.Task;
import com.lazy.todo.payload.request.SignupRequest;
import com.lazy.todo.payload.request.TaskRequest;
import com.lazy.todo.repository.TaskRepository;
import com.lazy.todo.security.jwt.JwtUtils;
import com.lazy.todo.services.TaskService;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import javax.transaction.TransactionScoped;
import javax.transaction.Transactional;

import java.util.Date;
import java.util.Optional;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.oneOf;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
public class SpringBootTodoTaskAcceptanceTests {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    Gson gson;

    @Value("${lazy.app.jwtSecret}")
    private String jwtSecret;

    @Value("${lazy.app.jwtExpirationMs}")
    private int jwtExpirationMs;

    TaskRequest taskRequest = new TaskRequest("title", "description");
    TaskRequest TASK_REQUEST_2 = new TaskRequest("title2", "description2");

    Task TASK_1 = new Task("title1", "description1");
    Task TASK_2 = new Task("title2", "description2");
    Task TASK_3 = new Task("title3", "description3");
    Task TASK_4 = new Task("title4", "description4");

    public String generateJwtToken() {

        return Jwts.builder()
                .setSubject(("testUser2"))
                .setIssuedAt(new Date())
                .setExpiration(new Date((new Date()).getTime() + jwtExpirationMs))
                .signWith(SignatureAlgorithm.HS512, jwtSecret)
                .compact();
    }

    @Test
    @Transactional
    @WithMockUser(username = "testUser2")
    public void newTaskTest() throws Exception {
        mockMvc.perform(
                        post("/api/task/new")
                                .contentType(MediaType.APPLICATION_JSON)
                                .header("Authorization", "bearer:" + generateJwtToken())
                                .content(gson.toJson(taskRequest))
                                .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title", is(taskRequest.getTitle())))
                .andReturn().getResponse().getContentAsString();
    }

    /**
     * KLUDGE!!! have manually entered a task here which fits this test requirement LIABLE TO BREAK
     * TODO add database setup instructions to readme
     *
     * @throws Exception
     */
    @Test
    @WithMockUser(username = "testUser2")
    public void getTaskByIdTest() throws Exception {
        String jwt = "bearer:" + generateJwtToken();
        mockMvc.perform(
                        get("/api/task/1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .header("Authorization", jwt)
                                .content(gson.toJson(taskRequest))
                                .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title", is(TASK_1.getTitle())))
                .andReturn().getResponse().getContentAsString();
    }

    @Test
    @WithMockUser(username = "testUser2")
    public void getAllTasksTest() throws Exception {
        String jwt = "bearer:" + generateJwtToken();
        mockMvc.perform(
                        get("/api/task/all")
                                .contentType(MediaType.APPLICATION_JSON)
                                .header("Authorization", jwt)
                                .content(gson.toJson(taskRequest))
                                .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.[0].title", oneOf("title", "title3",
                        "title4", "title2")))
                .andExpect(jsonPath("$.[1].title", oneOf("title", "title3",
                        "title4", "title2")))
                .andExpect(jsonPath("$.[2].title", oneOf("title", "title3",
                        "title4", "title2")))
                .andReturn().getResponse().getContentAsString();
    }


    @Test
    @WithMockUser(username = "testUser2")
    public void getTaskUsersTest() throws Exception {
        String jwt = "bearer:" + generateJwtToken();
        mockMvc.perform(
                        get("/api/task/users/1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .header("Authorization", jwt)
                                .content(gson.toJson(taskRequest))
                                .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.[0]", is("testUser2")))
                .andReturn().getResponse().getContentAsString();
    }

    @Test
    @WithMockUser(username = "testUser2")
    @Transactional
    public void getUpdateTaskTest() throws Exception {
        String jwt = "bearer:" + generateJwtToken();
        mockMvc.perform(
                        put("/api/task/1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .header("Authorization", jwt)
                                .content(gson.toJson(TASK_REQUEST_2))
                                .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title", is(TASK_REQUEST_2.getTitle())))
                .andReturn().getResponse().getContentAsString();
    }

    @Test
    @WithMockUser(username = "testUser2")
    @Transactional
    public void deleteTaskTest() throws Exception {
        String jwt = "bearer:" + generateJwtToken();
        mockMvc.perform(
                        delete("/api/task/1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .header("Authorization", jwt)
                                .content(gson.toJson(TASK_REQUEST_2))
                                .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title", is(taskRequest.getTitle())))
                .andReturn().getResponse().getContentAsString();
    }

}
