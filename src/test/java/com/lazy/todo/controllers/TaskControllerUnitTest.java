package com.lazy.todo.controllers;

import com.lazy.todo.exceptions.AccessDeniedException;
import com.lazy.todo.exceptions.NoSuchTaskException;
import com.lazy.todo.models.Task;
import com.lazy.todo.models.User;
import com.lazy.todo.payload.request.TaskRequest;
import com.lazy.todo.security.jwt.JwtUtils;
import com.lazy.todo.services.TaskService;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@WebMvcTest(TaskController.class)
class TaskControllerUnitTest {

    @Autowired
    TaskController taskController;

    @MockBean
    TaskService taskService;

    @MockBean
    JwtUtils jwtUtils;

    Task TASK_1 = new Task(LocalDateTime.now(), LocalDateTime.now(), LocalDateTime.now(), "title1", "description1");
    Task TASK_2 = new Task(LocalDateTime.now(), LocalDateTime.now(), LocalDateTime.now(), "title2", "description2");
    Task TASK_3 = new Task(LocalDateTime.now(), LocalDateTime.now(), LocalDateTime.now(), "title3", "description3");

    User USER_1 = new User("testusername1", "test@test.co.uk", "testpassword1");
    User USER_2 = new User("testusername2", "test@test.com", "testpassword2");


    @Test
    public void newTasksTest() {
        //test the "happy route" first
        TaskRequest taskRequest = new TaskRequest("testtitle", "testdescription");
        Task task = new Task(taskRequest.getTitle(), taskRequest.getDescription());
        when(jwtUtils.validateJwtToken("placeholderJwt")).thenReturn(true);
        when(taskService.saveNewTask("placeholderJwt", taskRequest)).thenReturn(task);
        //test that the controller correctly returns the value from taskService
        assertEquals(ResponseEntity.ok(task), taskController.newTasks("bearer placeholderJwt", taskRequest));
    }
    @Test
    public void newTasksBadJwtTest(){
        //now test malformed JWT, if jwtUtils does not validate the JWT we should get a 400 bad request
        TaskRequest taskRequest = new TaskRequest("testtitle", "testdescription");
        when(jwtUtils.validateJwtToken("placeholderJwt")).thenReturn(true);
        assertEquals(HttpStatus.BAD_REQUEST, taskController.newTasks("malformed JWT", taskRequest).getStatusCode());
    }

    @SneakyThrows
    @Test
    void getTaskById() {
        when(jwtUtils.validateJwtToken("placeholderJwt")).thenReturn(true);
        when(taskService.getTaskById("placeholderJwt", 1l)).thenReturn(TASK_1);
        //Test that the controller returns a responseentity containing the task returned by the taskservice
        assertEquals(ResponseEntity.ok(TASK_1), taskController.getTaskById(1L, "bearer placeholderJwt"));
    }
    @SneakyThrows
    @Test
    public void getTaskByIdAccessDeniedTest() {
        //test the case when taskservice returns Accessdenied
        when(jwtUtils.validateJwtToken("validJwtButWrongUser")).thenReturn(true);
        when(taskService.getTaskById("validJwtButWrongUser", 1l)).thenThrow(new AccessDeniedException());
        assertEquals(HttpStatus.UNAUTHORIZED, taskController.getTaskById(1L, "bearer validJwtButWrongUser").getStatusCode());
    }

    @SneakyThrows
    @Test
    public void getTaskByIdNoSuchTask() {
        //test the case when the task does not exist
        when(jwtUtils.validateJwtToken("placeholderJwt")).thenReturn(true);
        when(taskService.getTaskById("placeholderJwt", 2l)).thenThrow(new NoSuchTaskException());
        assertEquals(HttpStatus.NO_CONTENT, taskController.getTaskById(2L, "bearer placeholderJwt").getStatusCode());
    }

    @Test
    public void getTaskByIdMalformedJwtTest(){
        //test malformed JWT
        assertEquals(HttpStatus.BAD_REQUEST, taskController.getTaskById(1L, "malformedJwt").getStatusCode());
    }

    @Test
    public void getAllTasks() throws Exception {
        Set<Task> tasks = new HashSet<>(Arrays.asList(TASK_1, TASK_2, TASK_3));
        when(taskService.getAllTasks("placeholderJWT")).thenReturn(tasks);
        when(jwtUtils.validateJwtToken("placeholderJWT")).thenReturn(true);
        //check that we correctly return the value given by the taskService
        assertEquals(ResponseEntity
                .ok(tasks), taskController.getAllTasks("bearer placeholderJWT"));
    }

    @Test
    public void getAllTasksMalformedJwtTest(){
        //now test malformed jwt
        assertEquals(HttpStatus.BAD_REQUEST , taskController.getAllTasks("malformed JWT").getStatusCode());
    }

    @SneakyThrows
    @Test
    void getTaskUsers() {
        Set<String> users = new HashSet<>(Arrays.asList(USER_1.getUsername(), USER_2.getUsername()));
        when(jwtUtils.validateJwtToken("placeholderJwt")).thenReturn(true);
        when(taskService.getTaskUsers("placeholderJwt", 1L)).thenReturn(users);
        assertEquals(ResponseEntity
                .ok(users), taskController.getTaskUsers(1L, "bearer placeholderJwt"));
    }

    @Test
    public void getTaskUsersMalformedJwtTest() {
        //now test malformed JWT
        assertEquals(HttpStatus.BAD_REQUEST , taskController.getTaskUsers(1L,"malformed JWT").getStatusCode());
    }
    @SneakyThrows
    @Test
    public void getTaskUsersAccessDeniedTest() {
        //check that the controller correctly handles an access denied exceptioon from taskservice
        when(jwtUtils.validateJwtToken("placeholderJwt")).thenReturn(true);
        when(taskService.getTaskUsers("placeholderJwt", 2L)).thenThrow(new AccessDeniedException());
        assertEquals(HttpStatus.UNAUTHORIZED, taskController.getTaskUsers(2L, "Bearer placeholderJwt").getStatusCode());
    }

    @SneakyThrows
    @Test
    public void getTaskUsersNoSuchTaskTest() {
        //check that the controller correctly handles a no such task exception
        when(jwtUtils.validateJwtToken("placeholderJwt")).thenReturn(true);
        when(taskService.getTaskUsers("placeholderJwt", 2L)).thenThrow(new NoSuchTaskException());
        assertEquals(HttpStatus.NO_CONTENT, taskController.getTaskUsers(2L, "Bearer placeholderJwt").getStatusCode());
    }

    @SneakyThrows
    @Test
    public void updateTaskTest() {
        //check that taskController returns a task from the taskservice
        TaskRequest taskRequest = new TaskRequest("testtitle", "testdescription");
        Task task = new Task(taskRequest.getTitle(), taskRequest.getDescription());
        when(jwtUtils.validateJwtToken("placeholderJwt")).thenReturn(true);
        when(taskService.updateTaskById("placeholderJwt", 1L, taskRequest)).thenReturn(task);
        assertEquals(ResponseEntity.ok(task), taskController.updateTask(1L, "bearer placeholderJwt", taskRequest));
    }

    @Test
    public void updateTaskMalformedJwtTest() {
        TaskRequest taskRequest = new TaskRequest("testtitle", "testdescription");
        assertEquals(HttpStatus.BAD_REQUEST, taskController.updateTask(1L, "bearer placeholderJwt", taskRequest).getStatusCode());
    }

    @SneakyThrows
    @Test
    public void updateTaskAccessDeniedTest() {
        TaskRequest taskRequest = new TaskRequest("testtitle", "testdescription");
        when(jwtUtils.validateJwtToken("placeholderJwt")).thenReturn(true);
        when(taskService.updateTaskById("placeholderJwt", 2L, taskRequest)).thenThrow(new AccessDeniedException());
        assertEquals(HttpStatus.UNAUTHORIZED, taskController.updateTask(2L, "bearer placeholderJwt", taskRequest).getStatusCode());
    }

    @SneakyThrows
    @Test
    public void updateTaskNoSuchTaskTest() {
        TaskRequest taskRequest = new TaskRequest("testtitle", "testdescription");
        when(jwtUtils.validateJwtToken("placeholderJwt")).thenReturn(true);
        when(taskService.updateTaskById("placeholderJwt", 2L, taskRequest)).thenThrow(new NoSuchTaskException());
        assertEquals(HttpStatus.NO_CONTENT, taskController.updateTask(2L, "bearer placeholderJwt", taskRequest).getStatusCode());
    }

    @SneakyThrows
    @Test
    public void deleteTask() {
        when(jwtUtils.validateJwtToken("placeholderJwt")).thenReturn(true);
        Task task = new Task("testtile", "testdescription");
        when(taskService.deleteTaskById("placeholderJwt", 1L)).thenReturn(task);
        assertEquals(ResponseEntity.ok(task+" deleted"), taskController.deleteTask(1l, "bearer placeholderJwt"));
    }

    @Test
    public void deleteTaskMalformedjwtTest() {
        assertEquals(HttpStatus.BAD_REQUEST, taskController.deleteTask(1l, "malformed JWT").getStatusCode());
    }

    @SneakyThrows
    @Test
    public void deleteTaskAccessDeniedTest() {
        when(jwtUtils.validateJwtToken("placeholderJwt")).thenReturn(true);
        when(taskService.deleteTaskById("placeholderJwt", 1L)).thenThrow(new AccessDeniedException());
        assertEquals(HttpStatus.UNAUTHORIZED, taskController.deleteTask(1L, "bearer placeholderJwt").getStatusCode());
    }

    @SneakyThrows
    @Test
    public void deleteTaskNoSuchTaskTest() {
        when(jwtUtils.validateJwtToken("placeholderJwt")).thenReturn(true);
        when(taskService.deleteTaskById("placeholderJwt", 1L)).thenThrow(new NoSuchTaskException());
        assertEquals(HttpStatus.NO_CONTENT, taskController.deleteTask(1L, "bearer placeholderJwt").getStatusCode());
    }
}