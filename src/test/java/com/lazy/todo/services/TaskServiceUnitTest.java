package com.lazy.todo.services;

import com.lazy.todo.exceptions.AccessDeniedException;
import com.lazy.todo.exceptions.NoSuchTaskException;
import com.lazy.todo.models.Task;
import com.lazy.todo.models.User;
import com.lazy.todo.payload.request.TaskRequest;
import com.lazy.todo.repository.TaskRepository;
import com.lazy.todo.repository.UserRepository;
import com.lazy.todo.security.jwt.JwtUtils;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.PropertySource;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.context.web.WebAppConfiguration;

import javax.persistence.EntityManager;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;


@WebMvcTest(TaskService.class)
class TaskServiceUnitTest {

    @Autowired
    TaskService taskService;

    @MockBean
    JwtUtils jwtUtils;

    @MockBean
    UserRepository userRepository;

    @MockBean
    TaskRepository taskRepository;

    @MockBean
    EntityManager entityManager;

    Task TASK_1 = new Task(LocalDate.now(), LocalDate.now(), LocalDate.now(), "title1", "description1");
    Task TASK_2 = new Task(LocalDate.now(), LocalDate.now(), LocalDate.now(), "title2", "description2");
    Task TASK_3 = new Task(LocalDate.now(), LocalDate.now(), LocalDate.now(), "title3", "description3");

    User USER_1 = new User("testUserName1", "test@test.co.uk", "testPassword1");
    User USER_2 = new User("testUserName2", "test@test.com", "testPassword2");


    @Test
    void saveNewTask() {
        TaskRequest taskRequest = new TaskRequest("newTitle1", "newDescription1");
        when(jwtUtils.getUserNameFromJwtToken("placeholderJwt")).thenReturn(USER_1.getUsername());


        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(USER_1));
        when(taskRepository.save(any())).thenReturn(new Task(taskRequest.getTitle(), taskRequest.getDescription()));

        assertEquals(taskRequest.getTitle(),taskService.saveNewTask("placeholderJwt", taskRequest).getTitle());
        assertEquals(taskRequest.getDescription(),taskService.saveNewTask("placeholderJwt", taskRequest).getDescription());
    }

    @SneakyThrows
    @Test
    void getTaskByIdTest() {
        USER_1.getTasks().add(TASK_1);
        when(jwtUtils.getUserNameFromJwtToken("placeholderJwt")).thenReturn(USER_1.getUsername());
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(USER_1));
        when(taskRepository.findById((Long) any())).thenReturn(Optional.ofNullable(TASK_1));
        assertEquals(TASK_1,taskService.getTaskById("placeholderJwt", 1l));
    }
    @SneakyThrows
    @Test
    void getTaskByIdTaskNotOwnedTest() {
        when(jwtUtils.getUserNameFromJwtToken("placeholderJwt")).thenReturn(USER_1.getUsername());
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(USER_1));
        when(taskRepository.findById((Long) any())).thenReturn(Optional.ofNullable(TASK_1));
        assertThrows(AccessDeniedException.class, ()-> taskService.getTaskById("placeholderJwt", 1l));
    }

    @Test
    void getTaskByIdUserNotPresentTest() {
        when(jwtUtils.getUserNameFromJwtToken("placeholderJwt")).thenReturn(USER_1.getUsername());
        when(taskRepository.findById((Long) any())).thenReturn(Optional.ofNullable(TASK_1));
        assertThrows(UsernameNotFoundException.class, ()-> taskService.getTaskById("placeholderJwt", 1l));
    }

    @Test
    void getTaskByIdTaskNotPresentest() {
        when(jwtUtils.getUserNameFromJwtToken("placeholderJwt")).thenReturn(USER_1.getUsername());
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(USER_1));
        assertThrows(NoSuchTaskException.class, ()-> taskService.getTaskById("placeholderJwt", 1l));
    }

    @Test
    void getAllTasksTest() {
        when(jwtUtils.getUserNameFromJwtToken("placeholderJwt")).thenReturn(USER_1.getUsername());
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(USER_1));
        USER_1.getTasks().add(TASK_1);
        USER_1.getTasks().add(TASK_2);
        Set<Task> tasks = new HashSet<>(Arrays.asList(TASK_1, TASK_2));
        assertEquals(tasks, taskService.getAllTasks("placeholderJwt"));
    }

    @Test
    void getAllTasksUserNameNotFoundTest() {
        when(jwtUtils.getUserNameFromJwtToken("placeholderJwt")).thenReturn(USER_1.getUsername());
        assertThrows(UsernameNotFoundException.class, () -> taskService.getAllTasks("placeholderJwt"));
    }

    @SneakyThrows
    @Test
    void getTaskUsers() {
        TASK_1.getUsers().add(USER_1);
        USER_1.getTasks().add(TASK_1);
        Set<String> users = new HashSet<>(Arrays.asList(USER_1.getUsername()));
        when(jwtUtils.getUserNameFromJwtToken("placeholderJwt")).thenReturn(USER_1.getUsername());
        when(userRepository.findByUsername(USER_1.getUsername())).thenReturn(Optional.of(USER_1));
        when(taskRepository.findById(1L)).thenReturn(Optional.ofNullable(TASK_1));
        assertEquals(users, taskService.getTaskUsers("placeholderJwt", 1L));
    }

    @SneakyThrows
    @Test
    void getTaskUsersUserNotFound() {
        TASK_1.getUsers().add(USER_1);
        USER_1.getTasks().add(TASK_1);
        Set<String> users = new HashSet<>(Arrays.asList(USER_1.getUsername()));
        when(jwtUtils.getUserNameFromJwtToken("placeholderJwt")).thenReturn(USER_1.getUsername());
        when(taskRepository.findById(1L)).thenReturn(Optional.ofNullable(TASK_1));
        assertThrows(UsernameNotFoundException.class, () -> taskService.getTaskUsers("placeholderJwt", 1L));
    }

    @SneakyThrows
    @Test
    void getTaskUsersTaskNotFound() {
        TASK_1.getUsers().add(USER_1);
        USER_1.getTasks().add(TASK_1);
        Set<String> users = new HashSet<>(Arrays.asList(USER_1.getUsername()));
        when(jwtUtils.getUserNameFromJwtToken("placeholderJwt")).thenReturn(USER_1.getUsername());
        when(userRepository.findByUsername(USER_1.getUsername())).thenReturn(Optional.of(USER_1));
        assertThrows(NoSuchTaskException.class, () -> taskService.getTaskUsers("placeholderJwt", 1L));
    }

    @Test
    void getTaskUsersAccessDenied() {
        TASK_1.getUsers().add(USER_1);
        Set<String> users = new HashSet<>(Arrays.asList(USER_1.getUsername()));
        when(jwtUtils.getUserNameFromJwtToken("placeholderJwt")).thenReturn(USER_1.getUsername());
        when(userRepository.findByUsername(USER_1.getUsername())).thenReturn(Optional.of(USER_1));
        when(taskRepository.findById(1L)).thenReturn(Optional.ofNullable(TASK_1));
        assertThrows(AccessDeniedException.class, ()-> taskService.getTaskUsers("placeholderJwt", 1L));
    }

    @SneakyThrows
    @Test
    void updateTaskByIdTest() {
        TaskRequest taskRequest = new TaskRequest("changedTitle", "changedDescription");
        USER_1.getTasks().add(TASK_1);
        when(jwtUtils.getUserNameFromJwtToken("placeholderJwt")).thenReturn(USER_1.getUsername());
        when(userRepository.findByUsername(USER_1.getUsername())).thenReturn(Optional.ofNullable(USER_1));
        when(taskRepository.findById(1L)).thenReturn(Optional.ofNullable(TASK_1));
        assertEquals("changedTitle", taskService.updateTaskById("placeholderJwt", 1L, taskRequest).getTitle());
        assertEquals("changedDescription", taskService.updateTaskById("placeholderJwt", 1L, taskRequest).getDescription());
    }

    @Test
    void updateTaskByIdUserNotFoundTest() {
        TaskRequest taskRequest = new TaskRequest("changedTitle", "changedDescription");
        USER_1.getTasks().add(TASK_1);
        when(jwtUtils.getUserNameFromJwtToken("placeholderJwt")).thenReturn(USER_1.getUsername());
        when(taskRepository.findById(1L)).thenReturn(Optional.ofNullable(TASK_1));
        assertThrows(UsernameNotFoundException.class, () -> taskService.updateTaskById("placeholderJwt", 1L, taskRequest));
    }

    @Test
    void updateTaskByIdTaskNotFoundTest() {
        TaskRequest taskRequest = new TaskRequest("changedTitle", "changedDescription");
        USER_1.getTasks().add(TASK_1);
        when(jwtUtils.getUserNameFromJwtToken("placeholderJwt")).thenReturn(USER_1.getUsername());
        when(userRepository.findByUsername(USER_1.getUsername())).thenReturn(Optional.of(USER_1));
        assertThrows(NoSuchTaskException.class, () -> taskService.updateTaskById("placeholderJwt", 1L, taskRequest));
    }



    @Test
    void updateTaskByIdAccessDeniedTest() {
        TaskRequest taskRequest = new TaskRequest("changedTitle", "changedDescription");
        when(jwtUtils.getUserNameFromJwtToken("placeholderJwt")).thenReturn(USER_1.getUsername());
        when(userRepository.findByUsername(USER_1.getUsername())).thenReturn(Optional.of(USER_1));
        when(taskRepository.findById(1L)).thenReturn(Optional.ofNullable(TASK_1));
        assertThrows(AccessDeniedException.class, () -> taskService.updateTaskById("placeholderJwt", 1L, taskRequest));
    }

    @SneakyThrows
    @Test
    void deleteTaskByIdTest() {
        USER_1.getTasks().add(TASK_1);
        TASK_1.getUsers().add(USER_1);
        when(jwtUtils.getUserNameFromJwtToken("placeholderJwt")).thenReturn(USER_1.getUsername());
        when(userRepository.findByUsername(USER_1.getUsername())).thenReturn(Optional.of(USER_1));
        when(taskRepository.findById(1L)).thenReturn(Optional.ofNullable(TASK_1));
        assertFalse(USER_1.getTasks().contains(taskService.deleteTaskById("placeholderJwt", 1L)));
        assertFalse(USER_1.getTasks().contains(TASK_1));
    }

    @Test
    void deleteTaskByIdUserNotFoundTest() {
        USER_1.getTasks().add(TASK_1);
        TASK_1.getUsers().add(USER_1);
        when(jwtUtils.getUserNameFromJwtToken("placeholderJwt")).thenReturn(USER_1.getUsername());
        when(taskRepository.findById(1L)).thenReturn(Optional.ofNullable(TASK_1));
        assertThrows(UsernameNotFoundException.class, () -> taskService.deleteTaskById("placeholderJwt", 1L));

    }

    @Test
    void deleteTaskByIdTaskNotFoundTest() {
        USER_1.getTasks().add(TASK_1);
        TASK_1.getUsers().add(USER_1);
        when(jwtUtils.getUserNameFromJwtToken("placeholderJwt")).thenReturn(USER_1.getUsername());
        when(userRepository.findByUsername(USER_1.getUsername())).thenReturn(Optional.of(USER_1));
        assertThrows(NoSuchTaskException.class, () -> taskService.deleteTaskById("placeholderJwt", 1L));
    }

    @Test
    void deleteTaskByIdAccessDeniedTest() {
        TASK_1.getUsers().add(USER_1);
        when(jwtUtils.getUserNameFromJwtToken("placeholderJwt")).thenReturn(USER_1.getUsername());
        when(userRepository.findByUsername(USER_1.getUsername())).thenReturn(Optional.of(USER_1));
        when(taskRepository.findById(1L)).thenReturn(Optional.ofNullable(TASK_1));
        assertThrows(AccessDeniedException.class, () -> taskService.deleteTaskById("placeholderJwt", 1L));
    }

}