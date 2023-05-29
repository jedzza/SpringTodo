package com.lazy.todo.controllers;

import com.google.gson.*;
import com.lazy.todo.exceptions.AccessDeniedException;
import com.lazy.todo.exceptions.NoSuchTaskException;
import com.lazy.todo.models.Task;
import com.lazy.todo.models.User;
import com.lazy.todo.models.gson.LocalDateAdapter;
import com.lazy.todo.payload.request.TaskRequest;
import com.lazy.todo.security.jwt.JwtUtils;
import com.lazy.todo.services.AccountService;
import com.lazy.todo.services.TaskService;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.lang.reflect.Array;
import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TaskController.class)
@AutoConfigureMockMvc(addFilters = false)
class TaskControllerIntegrationTest {

    @Autowired
    TaskController taskController;

    @Autowired
    MockMvc mockMvc;

    Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
            .create();

    @MockBean
    JwtUtils jwtUtils;

    @MockBean
    TaskService taskService;

    @MockBean
    AccountService accountService;

    Task TASK_1 = new Task(LocalDate.now(), LocalDate.now(), LocalDate.now(), "title1", "description1");
    Task TASK_2 = new Task(LocalDate.now(), LocalDate.now(), LocalDate.now(), "title2", "description2");

    Set<Task> TASKS = new HashSet<>(Arrays.asList(TASK_1,TASK_2));

    User USER_1 = new User("testusername1", "test@test.co.uk", "testpassword1");
    User USER_2 = new User("testusername2", "test@test.com", "testpassword2");
    Set<String> USERNAMES = new HashSet<>(Arrays.asList(USER_1.getUsername(), USER_2.getUsername()));

    @SneakyThrows
    @Test
    void newTasksIntegrationTest() {
        TaskRequest taskRequest = new TaskRequest("title3", "description3");
        Task task = new Task(taskRequest.getTitle(), taskRequest.getDescription());
        when(taskService.saveNewTask(eq("placeholderJwt"), any()))
                .thenReturn(task);
        when(jwtUtils.validateJwtToken("placeholderJwt")).thenReturn(true);
        mockMvc.perform(
                post("/api/task/new")

                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "bearer:placeholderJwt")
                        .content(gson.toJson(taskRequest))
                        .accept(MediaType.APPLICATION_JSON))
                        .andDo(print())
                        .andExpect(status().isOk())
                .andExpect(jsonPath("$.title", is(task.getTitle())))
                .andReturn().getResponse().getContentAsString();
    }

    @SneakyThrows
    @Test
    void getTaskByIdIntegrationTest() {
        when(jwtUtils.validateJwtToken("placeholderJwt")).thenReturn(true);
        when(taskService.getTaskById("placeholderJwt", 1L)).thenReturn(TASK_1);
        mockMvc.perform(
                        get("/api/task/1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .header("Authorization", "bearer:placeholderJwt")
                                .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value(TASK_1.getTitle()))
                .andReturn().getResponse().getContentAsString();
    }


    @Test
    void getAllTasksIntegrationTest() throws Exception {
        when(jwtUtils.validateJwtToken("placeholderJwt")).thenReturn(true);
        when(taskService.getAllTasks("placeholderJwt")).thenReturn(TASKS);
        mockMvc.perform(
                        get("/api/task/all")
                                .contentType(MediaType.APPLICATION_JSON)
                                .header("Authorization", "bearer:placeholderJwt")
                                .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                //cannot guarantee order of the set so accept either option
                .andExpect(jsonPath("$.[0].title", anyOf(is(TASK_1.getTitle()), is(TASK_2.getTitle()))))
                .andExpect(jsonPath("$.[1].title", anyOf(is(TASK_1.getTitle()), is(TASK_2.getTitle()))))
                .andReturn().getResponse().getContentAsString();
    }

    @Test
    void getTaskUsersTest() throws Exception {
        when(jwtUtils.validateJwtToken("placeholderJwt")).thenReturn(true);
        when(taskService.getTaskUsers("placeholderJwt", 1L)).thenReturn(USERNAMES);
        mockMvc.perform(
                        get("/api/task/users/1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .header("Authorization", "bearer:placeholderJwt")
                                .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.[0]").value(USER_1.getUsername()))
                .andExpect(jsonPath("$.[1]").value(USER_2.getUsername()))
                .andReturn().getResponse().getContentAsString();
    }

    @Test
    void updateTaskTest() throws Exception {
        when(jwtUtils.validateJwtToken("placeholderJwt")).thenReturn(true);
        when(taskService.updateTaskById(eq("placeholderJwt"),eq(1L), any())).thenReturn(TASK_1);
        TaskRequest taskRequest = new TaskRequest("newTitle", "newDescription");
        mockMvc.perform(
                        put("/api/task/1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .header("Authorization", "bearer:placeholderJwt")
                                .content(gson.toJson(taskRequest))
                                .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value(TASK_1.getTitle()))
                .andReturn().getResponse().getContentAsString();
    }

    @Test
    void deleteTaskTest() throws Exception {
        when(jwtUtils.validateJwtToken("placeholderJwt")).thenReturn(true);
        when(taskService.deleteTaskById("placeholderJwt", 1L)).thenReturn(TASK_1);
        mockMvc.perform(
                        delete("/api/task/1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .header("Authorization", "bearer:placeholderJwt")
                                .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value(TASK_1.getTitle()))
                .andReturn().getResponse().getContentAsString();
    }
}