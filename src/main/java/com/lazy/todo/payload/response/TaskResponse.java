package com.lazy.todo.payload.response;

import com.lazy.todo.models.Task;
import com.lazy.todo.models.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TaskResponse {

    private String title;
    private String description;
    private LocalDateTime startDate;
    private LocalDateTime targetDate;
    private LocalDateTime checked;
    Set<User> users;
}
