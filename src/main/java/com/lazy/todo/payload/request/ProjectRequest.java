package com.lazy.todo.payload.request;

import com.lazy.todo.models.Project;
import com.lazy.todo.models.Task;
import com.lazy.todo.models.User;
import lombok.*;

import javax.swing.plaf.PanelUI;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.time.LocalDate;
import java.util.Set;

//the purpose of this task is to expose to the user those fields we wish them to be able to edit inside the Project POJO
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ProjectRequest {

    @NotBlank
    @Size(max = 100)
    private String title;

    private String description;

    private LocalDate startDate;

    private LocalDate targetDate;

    private LocalDate checked;

    public ProjectRequest(String title, String description) {
        this.title = title;
        this.description = description;
    }

}

