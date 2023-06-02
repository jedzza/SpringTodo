package com.lazy.todo.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Cascade;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
@NamedNativeQuery(name = "User.scoreCount",
        query = "SELECT Count(*)\n" +
                "FROM defaultdb.user_tasks ut \n" +
                "\tINNER JOIN defaultdb.users u\n" +
                "\t\tON ut.user_id = u.id\n" +
                "\tINNER JOIN defaultdb.tasks t\n" +
                "\t\tON ut.task_id = t.id\n" +
                "WHERE t.completed_on > (NOW() - INTERVAL 1 YEAR)\n" +
                "AND u.id = ?;")


@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "users", 
    uniqueConstraints = { 
      @UniqueConstraint(columnNames = "username"),
      @UniqueConstraint(columnNames = "email") 
    })
public class User {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @NotBlank
  @Size(max = 20)
  private String username;

  @NotBlank
  @Size(max = 50)
  @Email
  private String email;

  @NotBlank
  @Size(max = 120)
  private String password;

  private String personality = "a friendly coach";

  @ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
  @JoinTable(  name = "user_roles", 
        joinColumns = @JoinColumn(name = "user_id"), 
        inverseJoinColumns = @JoinColumn(name = "role_id"))
  private Set<Role> roles = new HashSet<>();

  @JsonIgnore
  @ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
  @JoinTable(  name = "user_tasks",
          joinColumns = @JoinColumn(name = "user_id"),
          inverseJoinColumns = @JoinColumn(name = "task_id"))
  private List<Task> tasks = new ArrayList<Task>();

  @OneToMany(mappedBy = "owner",
  cascade = CascadeType.ALL,
  orphanRemoval = true)
  private List<Project> projects;

  public User(String username, String email, String password) {
    this.username = username;
    this.email = email;
    this.password = password;
  }

}
