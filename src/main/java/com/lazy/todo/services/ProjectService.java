package com.lazy.todo.services;

import com.lazy.todo.exceptions.AccessDeniedException;
import com.lazy.todo.exceptions.NoSuchProjectException;
import com.lazy.todo.exceptions.NoSuchTaskException;
import com.lazy.todo.models.Project;
import com.lazy.todo.models.Task;
import com.lazy.todo.models.User;
import com.lazy.todo.payload.request.ProjectRequest;
import com.lazy.todo.payload.request.TaskRequest;
import com.lazy.todo.repository.ProjectRepository;
import com.lazy.todo.repository.TaskRepository;
import com.lazy.todo.repository.UserRepository;
import com.lazy.todo.security.jwt.JwtUtils;
import com.lazy.todo.services.TaskService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ProjectService {

    @Autowired
    JwtUtils jwtUtils;

    @Autowired
    UserRepository userRepository;

    @Autowired
    ProjectRepository projectRepository;

    @Autowired
    TaskService taskService;

    @Autowired
    TaskRepository taskRepository;

    public Project newProject(String jwt, ProjectRequest projectRequest) {

        String username = jwtUtils.getUserNameFromJwtToken(jwt); //TODO this should probably throw an error if unable to parse JWT
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User Not Found with username: " + username));
        Project project = new Project();
        BeanUtils.copyProperties(projectRequest, project);
        project.setOwner(user);
        return projectRepository.save(project);
    }

    public Project getProjectById(String jwt, Long id) throws NoSuchProjectException, AccessDeniedException {
        String usersname = jwtUtils.getUserNameFromJwtToken(jwt);
        User user = userRepository.findByUsername(usersname)
                .orElseThrow(() -> new UsernameNotFoundException("User Not Found With username: " + usersname));
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new NoSuchProjectException("Project Not Found With Id " + id));
        if (project.getOwner().getId() != user.getId()) {
            throw new AccessDeniedException("You don't have access to this project");
        } else {
            return project;
        }
    }

    public List<Project> getAllProjectsByUser(String jwt) {
        String username = jwtUtils.getUserNameFromJwtToken(jwt);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User Not Found with username: " + username));
        user.getProjects().sort(Comparator.comparing(Project::getPriority));
        return user.getProjects();
    }

    public List<Task> getProjectTasks(String jwt, Long projectId) throws NoSuchProjectException, AccessDeniedException {
        String username = jwtUtils.getUserNameFromJwtToken(jwt);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User Not Found with username: " + username));
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new NoSuchProjectException("no project found with id " + projectId));
        if (!user.getProjects().contains(project)) {
            throw new AccessDeniedException("You do not have access to this project");
        } else {
            //only send unfinished tasks and those finished today
            return user.getTasks().stream()
                    .filter(t -> (t.getCompletedOn().isAfter(LocalDate.now().minusDays(1))
                            || t.getChecked() == null)).collect(Collectors.toList());
        }
    }

    public Project updateProjectById(String jwt, Long id, ProjectRequest projectRequest) throws NoSuchProjectException, AccessDeniedException {
        String username = jwtUtils.getUserNameFromJwtToken(jwt);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User Not Found with userame: " + username));
        Project updatedProject = projectRepository.findById(id)
                .orElseThrow(() -> new NoSuchProjectException("Project Not Found with ID " + id));
        if (!user.getProjects().contains(updatedProject)) {
            throw new AccessDeniedException("You don't have access to requested resource");
        }
        BeanUtils.copyProperties(projectRequest, updatedProject);
        projectRepository.save(updatedProject);
        userRepository.save(user);
        return updatedProject;
    }

    public Project setProjectPriority(String jwt, Long id, int priority) throws NoSuchTaskException, AccessDeniedException {
        String username =jwtUtils.getUserNameFromJwtToken(jwt);
        User user =userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User Not Found with username " + username));
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new NoSuchTaskException("Task Not Found with task id " + id));
        if (!user.getProjects().contains(project)) {
            throw new AccessDeniedException("you do not have access to this task");
        }
        project.setPriority(priority);
        return projectRepository.save(project);
    }

    public Project deleteProjectById(String jwt, Long id) throws NoSuchProjectException, AccessDeniedException, NoSuchTaskException {
        String username = jwtUtils.getUserNameFromJwtToken(jwt);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User Not Found with userame: " + username));
        Project deletedProject = projectRepository.findById(id)
                .orElseThrow(() -> new NoSuchProjectException("No Project found with id " + id));
        if (!user.getProjects().contains(deletedProject)){
            throw new AccessDeniedException("You don't have access to this project");
        }
        user.getProjects().remove(deletedProject);
        for (Task task: deletedProject.getTasks()) {
            taskService.deleteTaskById(jwt, task.getId());
        }
        projectRepository.delete(id);
        return deletedProject;
    }

    public Project addTaskToProject(String jwt, Long projectId, TaskRequest taskRequest) throws NoSuchProjectException, AccessDeniedException {
        String username = jwtUtils.getUserNameFromJwtToken(jwt);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User Not Found with userame: " + username));
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new NoSuchProjectException("No Project found with id " + projectId));
        if (!user.getProjects().contains(project)){
            throw new AccessDeniedException("You don't have access to this project");
        }
        Task task = new Task();
        BeanUtils.copyProperties(taskRequest, task,"id");
        task.setProject(project);
        task.getUsers().add(user);
        taskRepository.save(task);
        return project;
    }
}