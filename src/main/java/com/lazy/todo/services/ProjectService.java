package com.lazy.todo.services;

import com.lazy.todo.exceptions.AccessDeniedException;
import com.lazy.todo.exceptions.NoSuchProjectException;
import com.lazy.todo.exceptions.NoSuchTaskException;
import com.lazy.todo.models.Project;
import com.lazy.todo.models.Task;
import com.lazy.todo.models.User;
import com.lazy.todo.payload.request.ProjectRequest;
import com.lazy.todo.repository.ProjectRepository;
import com.lazy.todo.repository.UserRepository;
import com.lazy.todo.security.jwt.JwtUtils;
import com.lazy.todo.services.TaskService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Set;

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

    public Set<Project> getAllProjectsByUser(String jwt) {
        String username = jwtUtils.getUserNameFromJwtToken(jwt);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User Not Found with username: " + username));
        return user.getProjects();
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
}