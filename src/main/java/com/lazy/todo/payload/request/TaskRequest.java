package com.lazy.todo.payload.request;

import com.lazy.todo.models.Project;
import lombok.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;

//the purpose of this task is to expose to the user those fields we wish them to be able to edit inside the Task POJO
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class TaskRequest {

    @NotBlank
    @Size(max = 100)
    private String title;

    private String description;

    private LocalDateTime startDate;

    private LocalDateTime targetDate;

    private LocalDateTime checked;

    private Project project;

    public TaskRequest(String title, String description) {
        this.title= title;
        this.description = description;
    }


    //define a constructor here to allow for creation of "headless" task without a project
    public TaskRequest(LocalDateTime startDate, LocalDateTime targetDate, LocalDateTime checked, String title,
                       String description){
        this.startDate = startDate;
        this.targetDate = targetDate;
        this.checked = checked;
        this.title = title;
        this.description = description;
    }
}
