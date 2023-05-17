package com.lazy.todo.services;

import com.lazy.todo.exceptions.AccessDeniedException;
import com.lazy.todo.exceptions.NoSuchProjectException;
import com.lazy.todo.models.Project;
import com.lazy.todo.models.Task;
import com.lazy.todo.models.User;
import com.lazy.todo.payload.request.ProjectRequest;
import com.lazy.todo.repository.ProjectRepository;
import com.lazy.todo.repository.UserRepository;
import com.lazy.todo.security.jwt.JwtUtils;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@WebMvcTest(ProjectService.class)
class ProjectServiceUnitTest {

    @MockBean
    JwtUtils jwtUtils;

    @MockBean
    UserRepository userRepository;

    @MockBean
    ProjectRepository projectRepository;

    @MockBean
    TaskService taskService;

    @Autowired
    ProjectService projectService;

    Task TASK_1 = new Task(LocalDate.now(), LocalDate.now(), LocalDate.now(), "title1", "description1");

    User USER_1 = new User("testUserName1", "test@test.co.uk", "testPassword1");

    User USER_2 = new User("testUserName2", "test@test.com", "testPassword2");

    Project PROJECT_1 = new Project("testTitle", "testDescription");

    Project PROJECT_2 = new Project("testTitle2", "testDescription2");

    Set<Project> projectList = new HashSet<>(Arrays.asList(PROJECT_1));

    Set<Task> tasks = new HashSet<>(Arrays.asList(TASK_1));

    @Test
    void newProjectTest() {
        ProjectRequest projectRequest = new ProjectRequest("NewTitle", "NewDescription");
        when(jwtUtils.getUserNameFromJwtToken("placeholderJwt")).thenReturn(USER_1.getUsername());
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(USER_1));
        when(projectRepository.save(any())).thenReturn(new Project(projectRequest.getTitle(), projectRequest.getDescription()));
        assertEquals(projectRequest.getTitle(), projectService.newProject("placeholderJwt", projectRequest).getTitle());
        assertEquals(projectRequest.getDescription(), projectService.newProject("placeholderJwt", projectRequest).getDescription());
    }

    @Test
    void newProjectUserNameNotFoundTest() {
        ProjectRequest projectRequest = new ProjectRequest("NewTitle", "NewDescription");
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(USER_1));
        when(projectRepository.save(any())).thenReturn(new Project(projectRequest.getTitle(), projectRequest.getDescription()));
        assertThrows(UsernameNotFoundException.class, () -> projectService.newProject("placeholderJwt", projectRequest));
    }

    @SneakyThrows
    @Test
    void getProjectByIdTest() {
        //ensure that users with Id's exist fot the function to compare to see if we are allowed access
        PROJECT_1.setOwner(USER_1);
        when(jwtUtils.getUserNameFromJwtToken("placeholderJwt")).thenReturn(USER_1.getUsername());
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(USER_1));
        when(projectRepository.findById(1l)).thenReturn(Optional.ofNullable(PROJECT_1));
        assertEquals(PROJECT_1, projectService.getProjectById("placeholderJwt", 1l));
    }

    @SneakyThrows
    @Test
    void getProjectByIdUsernameNotFoundTest() {
        PROJECT_1.setOwner(USER_1);
        when(jwtUtils.getUserNameFromJwtToken("placeholderJwt")).thenReturn(USER_1.getUsername());
        when(projectRepository.findById(1l)).thenReturn(Optional.ofNullable(PROJECT_1));
        assertThrows(UsernameNotFoundException.class, () -> projectService.getProjectById("placeholderJwt", 1l));
    }

    @Test
    void getProjectByIdProjectNotFoundTest() {
        PROJECT_1.setOwner(USER_1);
        when(jwtUtils.getUserNameFromJwtToken("placeholderJwt")).thenReturn(USER_1.getUsername());
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(USER_1));
        assertThrows(NoSuchProjectException.class, () -> projectService.getProjectById("placeholderJwt", 1l));
    }

    @Test
    void getProjectByIdAccessDeniedTest() {
        //ensure that users with Id's exist fot the function to compare to see if we are allowed access
        PROJECT_1.setOwner(USER_2);
        USER_1.setId(1L);
        USER_2.setId(2L);
        when(jwtUtils.getUserNameFromJwtToken("placeholderJwt")).thenReturn(USER_1.getUsername());
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(USER_1));
        when(projectRepository.findById(1l)).thenReturn(Optional.ofNullable(PROJECT_1));
        assertThrows(AccessDeniedException.class, () -> projectService.getProjectById("placeholderJwt", 1l));

    }

    @Test
    void getAllProjectsByUserTest() {
        USER_1.setProjects(projectList);
        when(jwtUtils.getUserNameFromJwtToken("placeholderJwt")).thenReturn(USER_1.getUsername());
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(USER_1));
        assertEquals(projectList, projectService.getAllProjectsByUser("placeholderJwt"));
    }

    @Test
    void getAllProjectsByUserUserNameNotFoundTest() {
        USER_1.setProjects(projectList);
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(USER_1));
        assertThrows(UsernameNotFoundException.class, () -> projectService.getAllProjectsByUser("placeholderJwt"));
    }


    @SneakyThrows
    @Test
    void updateProjectByIdTest() {
        USER_1.setProjects(projectList);
        ProjectRequest projectRequest = new ProjectRequest("NewTitle", "NewDescription");
        when(jwtUtils.getUserNameFromJwtToken("placeholderJwt")).thenReturn(USER_1.getUsername());
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(USER_1));
        when(projectRepository.findById(1l)).thenReturn(Optional.ofNullable(PROJECT_1));
        assertEquals(projectRequest.getTitle(), projectService.updateProjectById("placeholderJwt", 1L, projectRequest).getTitle());
        assertEquals(projectRequest.getDescription(), projectService.updateProjectById("placeholderJwt", 1L, projectRequest).getDescription());
    }

    @Test
    void updateProjectByIdUserNameNotFoundTest() {
        USER_1.setProjects(projectList);
        ProjectRequest projectRequest = new ProjectRequest("NewTitle", "NewDescription");
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(USER_1));
        when(projectRepository.findById(1l)).thenReturn(Optional.ofNullable(PROJECT_1));
        assertThrows(UsernameNotFoundException.class, () -> projectService.updateProjectById("placeholderJwt", 1L, projectRequest));
    }

    @Test
    void updateProjectByIdProjectNotFoundTest() {
        USER_1.setProjects(projectList);
        ProjectRequest projectRequest = new ProjectRequest("NewTitle", "NewDescription");
        when(jwtUtils.getUserNameFromJwtToken("placeholderJwt")).thenReturn(USER_1.getUsername());
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(USER_1));
        assertThrows(NoSuchProjectException.class, () -> projectService.updateProjectById("placeholderJwt", 1L, projectRequest));

    }

    @Test
    void updateProjectByIdAccessDeniedTest() {
        USER_1.setProjects(projectList);
        ProjectRequest projectRequest = new ProjectRequest("NewTitle", "NewDescription");
        when(jwtUtils.getUserNameFromJwtToken("placeholderJwt")).thenReturn(USER_1.getUsername());
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(USER_1));
        when(projectRepository.findById(2l)).thenReturn(Optional.ofNullable(PROJECT_2));
        assertThrows(AccessDeniedException.class, () -> projectService.updateProjectById("placeholderJwt", 2L, projectRequest));
    }

    @SneakyThrows
    @Test
    void deleteProjectByIdTest() {
        USER_1.setProjects(projectList);
        PROJECT_1.setTasks(tasks);
        when(jwtUtils.getUserNameFromJwtToken("placeholderJwt")).thenReturn(USER_1.getUsername());
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(USER_1));
        when(projectRepository.findById(1l)).thenReturn(Optional.ofNullable(PROJECT_1));
        assertEquals(PROJECT_1, projectService.deleteProjectById("placeholderJwt", 1l));
    }

    @Test
    void deleteProjectByIdUserNameNotFoundTest() {
        USER_1.setProjects(projectList);
        PROJECT_1.setTasks(tasks);
        when(jwtUtils.getUserNameFromJwtToken("placeholderJwt")).thenReturn(USER_1.getUsername());
        when(projectRepository.findById(1l)).thenReturn(Optional.ofNullable(PROJECT_1));
        assertThrows(UsernameNotFoundException.class, () -> projectService.deleteProjectById("placeholderJwt", 1l));
    }

    @Test
    void deleteProjectByIdProjectNotFoundTest() {
        USER_1.setProjects(projectList);
        PROJECT_1.setTasks(tasks);
        when(jwtUtils.getUserNameFromJwtToken("placeholderJwt")).thenReturn(USER_1.getUsername());
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(USER_1));;
        assertThrows(NoSuchProjectException.class, () -> projectService.deleteProjectById("placeholderJwt", 1l));
    }


    @Test
    void deleteProjectByIdAccessDeniedTest() {
        USER_1.setProjects(projectList);
        PROJECT_1.setTasks(tasks);
        when(jwtUtils.getUserNameFromJwtToken("placeholderJwt")).thenReturn(USER_1.getUsername());
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(USER_1));
        when(projectRepository.findById(2l)).thenReturn(Optional.ofNullable(PROJECT_2));
        assertThrows(AccessDeniedException.class, () -> projectService.deleteProjectById("placeholderJwt", 2l));
    }
}