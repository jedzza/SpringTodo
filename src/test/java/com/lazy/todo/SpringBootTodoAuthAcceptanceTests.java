package com.lazy.todo;

import com.google.gson.Gson;
import com.lazy.todo.models.Task;
import com.lazy.todo.payload.request.LoginRequest;
import com.lazy.todo.payload.request.SignupRequest;
import com.lazy.todo.payload.request.TaskRequest;
import com.lazy.todo.repository.UserRepository;
import com.lazy.todo.security.jwt.JwtUtils;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import javax.transaction.Transactional;
import java.util.Date;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class SpringBootTodoAuthAcceptanceTests {

	@Autowired
	MockMvc mockMvc;

	@Autowired
	UserRepository userRepository;

	@Autowired
	AuthenticationManager authenticationManager;

	@Autowired
	JwtUtils jwtUtils;

	@Autowired
	Gson gson;

	@Value("${lazy.app.jwtSecret}")
	private String jwtSecret;

	@Value("${lazy.app.jwtExpirationMs}")
	private int jwtExpirationMs;


	TaskRequest taskRequest = new TaskRequest("title3", "description3");
	SignupRequest signupRequest = new SignupRequest("testUser3", "test@test3.com", "password");
	SignupRequest signupRequest2 = new SignupRequest("testUser2", "test@test.com", "password");

	LoginRequest loginRequest = new LoginRequest("testUser2", "password");

	public String generateJwtToken() {

		return Jwts.builder()
				.setSubject(("testUser2"))
				.setIssuedAt(new Date())
				.setExpiration(new Date((new Date()).getTime() + jwtExpirationMs))
				.signWith(SignatureAlgorithm.HS512, jwtSecret)
				.compact();
	}

	@Test
	@Order(1)
	@Transactional
	public void happyRegisterTest() throws Exception {
		Task task = new Task(taskRequest.getTitle(), taskRequest.getDescription());
		mockMvc.perform(
						post("/api/auth/signup")
								.contentType(MediaType.APPLICATION_JSON)
								.header("Authorization", "bearer:placeholderJwt")
								.content(gson.toJson(signupRequest))
								.accept(MediaType.APPLICATION_JSON))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.message", is("User registered successfully!")))
				.andReturn().getResponse().getContentAsString();
		}
/*
on a first install, this test will fail as testuser2 will be added to the database. It should pass from then on
 */
	@Test
	@Order(2)
	public void RegisterUsernameAlreadyExistsTest() throws Exception {
		mockMvc.perform(
						post("/api/auth/signup")
								.contentType(MediaType.APPLICATION_JSON)
								.header("Authorization", "bearer:placeholderJwt")
								.content(gson.toJson(signupRequest2))
								.accept(MediaType.APPLICATION_JSON))
				.andDo(print())
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message", is("Error: Username is already taken!")))
				.andReturn().getResponse().getContentAsString();
	}

	@Test
	@Order(3)
	public void loginTest() throws Exception {
		LoginRequest loginRequest = new LoginRequest("testUser2", "password");
		mockMvc.perform(
						post("/api/auth/signin")
								.contentType(MediaType.APPLICATION_JSON)
								.header("Authorization", "bearer:placeholderJwt")
								.content(gson.toJson(signupRequest2))
								.accept(MediaType.APPLICATION_JSON))
				.andDo(print())
				.andExpect(status().isOk())
				.andReturn().getResponse().getContentAsString();

	}
	@Test
	@Order(4)
	@Transactional
	@WithMockUser(username = "testUser2")
	public void deleteTest() throws Exception {
		mockMvc.perform(
						delete("/api/account")
								.contentType(MediaType.APPLICATION_JSON)
								.header("Authorization", "bearer:" + generateJwtToken())
								.accept(MediaType.APPLICATION_JSON))
				.andDo(print())
				.andExpect(status().isOk())
				.andReturn().getResponse().getContentAsString();
		mockMvc.perform(
						delete("/api/account")
								.contentType(MediaType.APPLICATION_JSON)
								.header("Authorization", "bearer:" + generateJwtToken())
								.accept(MediaType.APPLICATION_JSON))
				.andDo(print())
				.andExpect(status().isNoContent())
				.andReturn().getResponse().getContentAsString();
	}
}
