package com.lazy.todo.controllers;

import com.lazy.todo.exceptions.AccessDeniedException;
import com.lazy.todo.exceptions.NoSuchProjectException;
import com.lazy.todo.exceptions.NoSuchTaskException;
import com.lazy.todo.models.Project;
import com.lazy.todo.models.Task;
import com.lazy.todo.models.User;
import com.lazy.todo.payload.request.ProjectRequest;
import com.lazy.todo.payload.request.TaskRequest;
import com.lazy.todo.security.jwt.JwtUtils;
import com.lazy.todo.services.ProjectService;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@WebMvcTest(ProjectController.class)
class ProjectControllerUnitTest {

    @Autowired
    ProjectController projectController;

    @MockBean
    JwtUtils jwtUtils;

    @MockBean
    ProjectService projectService;

    Project PROJECT_1 = new Project("title1", "description1");
    Project PROJECT_2 = new Project("title2", "description2");
    Project PROJECT_3 = new Project("title3", "description3");

    Task TASK_1 = new Task("taskTitle", "taskDescription");

    Set<Project> PROJECT_LIST = new HashSet<Project>(Arrays.asList(PROJECT_1, PROJECT_2, PROJECT_3));

    User USER_1 = new User("testusername1", "test@test.co.uk", "testpassword1");
    User USER_2 = new User("testusername2", "test@test.com", "testpassword2");
    ProjectRequest PROJECT_REQUEST_1 = new ProjectRequest("testTitle", "testDescription");
    TaskRequest TASK_REQUEST_1 = new TaskRequest("testTitle", "testDescription");


    @Test
    void newProjectTestTest() {
        Project project = new Project(PROJECT_REQUEST_1.getTitle(),PROJECT_REQUEST_1.getDescription());
        when(jwtUtils.validateJwtToken("placeholderJwt")).thenReturn(true);
        when(projectService.newProject("placeholderJwt", PROJECT_REQUEST_1)).thenReturn(project);
        assertEquals(ResponseEntity.ok(project), projectController.newProject("bearer placeholderJwt", PROJECT_REQUEST_1));
    }

    @Test
    void newProjectBadJwtTest() {
        Project project = new Project(PROJECT_REQUEST_1.getTitle(),PROJECT_REQUEST_1.getDescription());
        when(jwtUtils.validateJwtToken("placeholderJwt")).thenReturn(true);
        when(projectService.newProject("placeholderJwt", PROJECT_REQUEST_1)).thenReturn(project);
        assertEquals(HttpStatus.BAD_REQUEST, projectController.newProject("bearer BadJwt", PROJECT_REQUEST_1).getStatusCode());
    }

    @SneakyThrows
    @Test
    void getProjectByIdTest() {
        when(jwtUtils.validateJwtToken("placeholderJwt")).thenReturn(true);
        when(projectService.getProjectById("placeholderJwt", 1l)).thenReturn(PROJECT_1);
        assertEquals(ResponseEntity.ok(PROJECT_1), projectController.getProjectById(1L,"bearer placeholderJwt"));
    }

    @SneakyThrows
    @Test
    void getProjectByIdBadJwtTest() {
        when(jwtUtils.validateJwtToken("placeholderJwt")).thenReturn(true);
        when(projectService.getProjectById("placeholderJwt", 1l)).thenReturn(PROJECT_1);
        assertEquals(HttpStatus.BAD_REQUEST, projectController.getProjectById(1L,"bearer badJwt").getStatusCode());
    }

    @SneakyThrows
    @Test
    void getProjectByIdAccessDeniedTest() {
        when(jwtUtils.validateJwtToken("placeholderJwt")).thenReturn(true);
        when(projectService.getProjectById("placeholderJwt", 2l)).thenThrow(new AccessDeniedException());
        assertEquals(HttpStatus.UNAUTHORIZED, projectController.getProjectById(2L, "bearer placeholderJwt").getStatusCode());
    }

    @SneakyThrows
    @Test
    void getProjectByIdNoContentTest(){
        when(jwtUtils.validateJwtToken("placeholderJwt")).thenReturn(true);
        when(projectService.getProjectById("placeholderJwt", 2l)).thenThrow(new NoSuchProjectException());
        assertEquals(HttpStatus.NO_CONTENT, projectController.getProjectById(2L, "bearer placeholderJwt").getStatusCode());
    }

    @Test
    void getAllProjectsTest() {
        when(jwtUtils.validateJwtToken("placeholderJwt")).thenReturn(true);
        when(projectService.getAllProjectsByUser("placeholderJwt")).thenReturn(PROJECT_LIST);
        assertEquals(ResponseEntity.ok(PROJECT_LIST), projectController.getAllProjects("bearer placeholderJwt"));
    }

    @Test
    void getAllProjectsBadJwtTest() {
        when(jwtUtils.validateJwtToken("placeholderJwt")).thenReturn(true);
        assertEquals(HttpStatus.BAD_REQUEST, projectController.getAllProjects("bearer BadJwt").getStatusCode());
    }

    @SneakyThrows
    @Test
    void updateProjectById() {
        ProjectRequest projectRequest = new ProjectRequest();
        when(jwtUtils.validateJwtToken("placeholderJwt")).thenReturn(true);
        when(projectService.updateProjectById("placeholderJwt",1L, projectRequest)).thenReturn(PROJECT_2);
        assertEquals(ResponseEntity.ok(PROJECT_2), projectController.updateProjectById(1L, "bearer placeholderJwt", projectRequest));
    }

    @Test
    void updateProjectByIdBadJwtTest() {
        ProjectRequest projectRequest = new ProjectRequest();
        when(jwtUtils.validateJwtToken("placeholderJwt")).thenReturn(true);
        assertEquals(HttpStatus.BAD_REQUEST, projectController.updateProjectById(1l, "bearer badJwt", projectRequest).getStatusCode());
    }

    @SneakyThrows
    @Test
    void updateProjectByIdAccessDeniedTest() {
        ProjectRequest projectRequest = new ProjectRequest();
        when(jwtUtils.validateJwtToken("placeholderWrongJwt")).thenReturn(true);
        when(projectService.updateProjectById("placeholderWrongJwt",1L, projectRequest)).thenThrow(new AccessDeniedException());
        assertEquals(HttpStatus.UNAUTHORIZED, projectController.updateProjectById(1L, "bearer placeholderWrongJwt", projectRequest).getStatusCode());
    }

    @SneakyThrows
    @Test
    void updateProjectByIdNoContentTest() {
        ProjectRequest projectRequest = new ProjectRequest();
        when(jwtUtils.validateJwtToken("placeholderWrongJwt")).thenReturn(true);
        when(projectService.updateProjectById("placeholderWrongJwt",1L, projectRequest)).thenThrow(new NoSuchProjectException());
        assertEquals(HttpStatus.NO_CONTENT, projectController.updateProjectById(1L, "bearer placeholderWrongJwt", projectRequest).getStatusCode());
    }

    @SneakyThrows
    @Test
    void deleteProjectById() {
        when(jwtUtils.validateJwtToken("placeholderJwt")).thenReturn(true);
        when(projectService.deleteProjectById("placeholderJwt", 1l)).thenReturn(PROJECT_1);
        assertEquals(ResponseEntity.ok(PROJECT_1), projectController.deleteProjectById(1L, "bearer placeholderJwt"));
    }

    @Test
    void deleteProjectByIdBadJwtTest() {
        when(jwtUtils.validateJwtToken("placeholderJwt")).thenReturn(true);
        assertEquals(HttpStatus.BAD_REQUEST,projectController.deleteProjectById(1L, "bearer badJwt").getStatusCode());
    }

    @SneakyThrows
    @Test
    void deleteProjectByIdAccessDeniedTest() {
        when(jwtUtils.validateJwtToken("placeholderJwt")).thenReturn(true);
        when(projectService.deleteProjectById("placeholderJwt", 1l)).thenThrow(new AccessDeniedException());
        assertEquals(HttpStatus.UNAUTHORIZED, projectController.deleteProjectById(1L, "bearer placeholderJwt").getStatusCode());
    }

    @SneakyThrows
    @Test
    void deleteProjectByIdNoContentTest() {
        when(jwtUtils.validateJwtToken("placeholderJwt")).thenReturn(true);
        when(projectService.deleteProjectById("placeholderJwt", 1l)).thenThrow(new NoSuchTaskException());
        assertEquals(HttpStatus.NO_CONTENT, projectController.deleteProjectById(1L, "bearer placeholderJwt").getStatusCode());
    }

    @SneakyThrows
    @Test
    void addTaskToProjectTest() {
        when(jwtUtils.validateJwtToken("placeholderJwt")).thenReturn(true);
        when(projectService.addTaskToProject("placeholderJwt", 1L, TASK_REQUEST_1)).thenReturn(PROJECT_1);
        assertEquals(ResponseEntity.ok(PROJECT_1), projectController.addTaskToProject(1L, "bearer:placeholderJwt", TASK_REQUEST_1));
    }
    @SneakyThrows
    @Test
    void addTaskToProjectNoSuchTaskTest() {
        when(jwtUtils.validateJwtToken("placeholderJwt")).thenReturn(true);
        when(projectService.addTaskToProject("placeholderJwt", 1L, TASK_REQUEST_1)).thenThrow(new NoSuchProjectException());
        assertEquals(HttpStatus.NO_CONTENT, projectController.addTaskToProject(1L, "bearer:placeholderJwt", TASK_REQUEST_1).getStatusCode());
    }

    @SneakyThrows
    @Test
    void addTaskToProjectAccessDeniedTest() {
        when(jwtUtils.validateJwtToken("placeholderJwt")).thenReturn(true);
        when(projectService.addTaskToProject("placeholderJwt", 1L, TASK_REQUEST_1)).thenThrow(new AccessDeniedException());
        assertEquals(HttpStatus.UNAUTHORIZED, projectController.addTaskToProject(1L, "bearer:placeholderJwt", TASK_REQUEST_1).getStatusCode());
    }
}