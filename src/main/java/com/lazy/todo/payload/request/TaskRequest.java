package com.lazy.todo.payload.request;

import com.lazy.todo.models.Project;
import lombok.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.time.LocalDate;

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

    private LocalDate startDate;

    private LocalDate targetDate;

    private LocalDate checked;

    private Project project;

    private int priority;

    private int projectPriority;

    public TaskRequest(String title, String description) {
        this.title= title;
        this.description = description;
    }


    //define a constructor here to allow for creation of "headless" task without a project
    public TaskRequest(LocalDate startDate, LocalDate targetDate, LocalDate checked, String title,
                       String description){
        this.startDate = startDate;
        this.targetDate = targetDate;
        this.checked = checked;
        this.title = title;
        this.description = description;
    }

}
