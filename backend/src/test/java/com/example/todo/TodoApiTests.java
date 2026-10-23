package com.example.todo;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class TodoApiTests {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void createsTogglesAndDeletesATodo() throws Exception {
		String body = mockMvc.perform(post("/api/todos")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"title\":\"Buy milk\"}"))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.title").value("Buy milk"))
				.andExpect(jsonPath("$.completed").value(false))
				.andReturn()
				.getResponse()
				.getContentAsString();
		String id = body.replaceAll(".*\"id\"\\s*:\\s*\"([^\"]+)\".*", "$1");

		mockMvc.perform(patch("/api/todos/" + id)
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"completed\":true}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.completed").value(true))
				.andExpect(jsonPath("$.title").value("Buy milk"));

		mockMvc.perform(get("/api/todos"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.items[?(@.id=='" + id + "')]").exists());

		mockMvc.perform(delete("/api/todos/" + id)).andExpect(status().isNoContent());
		mockMvc.perform(get("/api/todos"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.items[?(@.id=='" + id + "')]").doesNotExist());
	}

	@Test
	void rejectsABlankTitle() throws Exception {
		mockMvc.perform(post("/api/todos")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"title\":\"  \"}"))
				.andExpect(status().isBadRequest());
	}

	@Test
	void returnsNotFoundForAnUnknownId() throws Exception {
		mockMvc.perform(delete("/api/todos/does-not-exist")).andExpect(status().isNotFound());
	}
}
