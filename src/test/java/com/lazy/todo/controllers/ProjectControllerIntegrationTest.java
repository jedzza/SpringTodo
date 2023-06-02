package com.lazy.todo.controllers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.lazy.todo.exceptions.AccessDeniedException;
import com.lazy.todo.exceptions.NoSuchProjectException;
import com.lazy.todo.exceptions.NoSuchTaskException;
import com.lazy.todo.models.Project;
import com.lazy.todo.models.Task;
import com.lazy.todo.models.User;
import com.lazy.todo.models.gson.LocalDateAdapter;
import com.lazy.todo.payload.request.ProjectRequest;
import com.lazy.todo.payload.request.TaskRequest;
import com.lazy.todo.security.jwt.JwtUtils;
import com.lazy.todo.services.ProjectService;
import com.lazy.todo.services.SortingService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.*;

import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProjectController.class)
@AutoConfigureMockMvc(addFilters = false)
class ProjectControllerIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ProjectController projectController;

    @MockBean
    JwtUtils jwtUtils;

    @MockBean
    SortingService sortingService;

    @MockBean
    ProjectService projectService;

    Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
            .create();

    Project PROJECT_1 = new Project("title1", "description1");
    Project PROJECT_2 = new Project("title2", "description2");
    Project PROJECT_3 = new Project("title3", "description3");

    List<Project> PROJECT_LIST = new ArrayList<>(Arrays.asList(PROJECT_1, PROJECT_2, PROJECT_3));

    User USER_1 = new User("testusername1", "test@test.co.uk", "testpassword1");
    User USER_2 = new User("testusername2", "test@test.com", "testpassword2");
    ProjectRequest PROJECT_REQUEST_1 = new ProjectRequest("testTitle", "testDescription");


    @Test
    void newProjectIntegrationTest() throws Exception {
        ProjectRequest projectRequest = new ProjectRequest("title4", "description4");
        Project project = new Project(projectRequest.getTitle(), projectRequest.getDescription());
        when(projectService.newProject(eq("placeholderJwt"), any()))
                .thenReturn(project);
        when(jwtUtils.validateJwtToken("placeholderJwt")).thenReturn(true);
        mockMvc.perform(
                        post("/api/project/new")
                                .contentType(MediaType.APPLICATION_JSON)
                                .header("Authorization", "bearer:placeholderJwt")
                                .content(gson.toJson(projectRequest))
                                .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title", is(project.getTitle())))
                .andReturn().getResponse().getContentAsString();
    }

    @Test
    void getProjectByIdIntegrationTest() throws Exception {
        when(jwtUtils.validateJwtToken("placeholderJwt")).thenReturn(true);
        when(projectService.getProjectById("placeholderJwt", 1L)).thenReturn(PROJECT_1);
        mockMvc.perform(
                        get("/api/project/1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .header("Authorization", "bearer:placeholderJwt")
                                .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title", is(PROJECT_1.getTitle())))
                .andReturn().getResponse().getContentAsString();
    }

    @Test
    void getAllProjectsIntegrationTest() throws Exception {
        when(jwtUtils.validateJwtToken("placeholderJwt")).thenReturn(true);
        when(projectService.getAllProjectsByUser("placeholderJwt")).thenReturn(PROJECT_LIST);
        mockMvc.perform(
                        get("/api/project/all")
                                .contentType(MediaType.APPLICATION_JSON)
                                .header("Authorization", "bearer:placeholderJwt")
                                .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.[0].title", anyOf(is(PROJECT_1.getTitle()), is(PROJECT_2.getTitle()), is(PROJECT_3.getTitle()))))
                .andExpect(jsonPath("$.[1].title", anyOf(is(PROJECT_1.getTitle()), is(PROJECT_2.getTitle()), is(PROJECT_3.getTitle()))))
                .andReturn().getResponse().getContentAsString();
    }


    @Test
    void updateProjectByIdIntegrationTest() throws Exception {
        when(jwtUtils.validateJwtToken("placeholderJwt")).thenReturn(true);
        when(projectService.updateProjectById(eq("placeholderJwt"), eq(1L), any())).thenReturn(PROJECT_1);
        mockMvc.perform(
                        put("/api/project/1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .header("Authorization", "bearer:placeholderJwt")
                                .content(gson.toJson(PROJECT_REQUEST_1))
                                .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title", is(PROJECT_1.getTitle())))
                .andReturn().getResponse().getContentAsString();
    }

    @Test
    void deleteProjectByIdIntegrationTest() throws Exception {
        when(jwtUtils.validateJwtToken("placeholderJwt")).thenReturn(true);
        when(projectService.deleteProjectById("placeholderJwt", 1L)).thenReturn(PROJECT_1);
        mockMvc.perform(
                        delete("/api/project/1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .header("Authorization", "bearer:placeholderJwt")
                                .content(gson.toJson(PROJECT_REQUEST_1))
                                .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title", is(PROJECT_1.getTitle())))
                .andReturn().getResponse().getContentAsString();
    }
}