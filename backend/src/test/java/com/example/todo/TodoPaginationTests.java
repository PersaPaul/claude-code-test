package com.example.todo;

import com.example.todo.domain.TodoRepository;
import org.junit.jupiter.api.BeforeEach;
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
class TodoPaginationTests {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private TodoRepository repository;

	@BeforeEach
	void reset() throws Exception {
		repository.findAll().forEach(todo -> repository.deleteById(todo.id()));
		for (int i = 1; i <= 14; i++) {
			mockMvc.perform(post("/api/todos")
					.contentType(MediaType.APPLICATION_JSON)
					.content("{\"title\":\"Task %d\"}".formatted(i)));
		}
	}

	@Test
	void firstPageHoldsTheDefaultSizeAndReportsTotals() throws Exception {
		mockMvc.perform(get("/api/todos"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.items.length()").value(10))
				.andExpect(jsonPath("$.items[0].title").value("Task 1"))
				.andExpect(jsonPath("$.page").value(0))
				.andExpect(jsonPath("$.size").value(10))
				.andExpect(jsonPath("$.totalItems").value(14))
				.andExpect(jsonPath("$.totalPages").value(2))
				.andExpect(jsonPath("$.activeCount").value(14))
				.andExpect(jsonPath("$.completedCount").value(0));
	}

	@Test
	void lastPageHoldsTheRemainder() throws Exception {
		mockMvc.perform(get("/api/todos").param("page", "1"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.items.length()").value(4))
				.andExpect(jsonPath("$.items[0].title").value("Task 11"))
				.andExpect(jsonPath("$.page").value(1));
	}

	@Test
	void pagesBeyondTheEndClampToTheLastPage() throws Exception {
		mockMvc.perform(get("/api/todos").param("page", "99").param("size", "5"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.page").value(2))
				.andExpect(jsonPath("$.totalPages").value(3))
				.andExpect(jsonPath("$.items.length()").value(4));
	}

	@Test
	void anEmptyResultStillReportsOnePage() throws Exception {
		mockMvc.perform(get("/api/todos").param("filter", "completed"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.items.length()").value(0))
				.andExpect(jsonPath("$.totalItems").value(0))
				.andExpect(jsonPath("$.totalPages").value(1));
	}

	@Test
	void filteringNarrowsItemsButCountsStayGlobal() throws Exception {
		String id = repository.findAll().get(0).id();
		mockMvc.perform(patch("/api/todos/" + id)
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"completed\":true}"));

		mockMvc.perform(get("/api/todos").param("filter", "completed"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.items.length()").value(1))
				.andExpect(jsonPath("$.totalItems").value(1))
				// Counts describe every todo, not just the filtered page.
				.andExpect(jsonPath("$.activeCount").value(13))
				.andExpect(jsonPath("$.completedCount").value(1));

		mockMvc.perform(get("/api/todos").param("filter", "active"))
				.andExpect(jsonPath("$.totalItems").value(13))
				.andExpect(jsonPath("$.totalPages").value(2));
	}

	@Test
	void filterIsCaseInsensitive() throws Exception {
		mockMvc.perform(get("/api/todos").param("filter", "active")).andExpect(status().isOk());
		mockMvc.perform(get("/api/todos").param("filter", "ACTIVE")).andExpect(status().isOk());
		mockMvc.perform(get("/api/todos").param("filter", "Active")).andExpect(status().isOk());
	}

	@Test
	void rejectsOutOfRangePagingParameters() throws Exception {
		mockMvc.perform(get("/api/todos").param("page", "-1")).andExpect(status().isBadRequest());
		mockMvc.perform(get("/api/todos").param("size", "0")).andExpect(status().isBadRequest());
		mockMvc.perform(get("/api/todos").param("size", "101")).andExpect(status().isBadRequest());
		mockMvc.perform(get("/api/todos").param("filter", "bogus")).andExpect(status().isBadRequest());
	}

	@Test
	void deletingTheOnlyItemOnTheLastPageClampsBack() throws Exception {
		// 11 todos at size 10 -> page 1 holds exactly one item.
		for (int i = 0; i < 3; i++) {
			String id = repository.findAll().get(repository.findAll().size() - 1).id();
			mockMvc.perform(delete("/api/todos/" + id)).andExpect(status().isNoContent());
		}
		mockMvc.perform(get("/api/todos").param("page", "1"))
				.andExpect(jsonPath("$.totalItems").value(11))
				.andExpect(jsonPath("$.items.length()").value(1));

		String last = repository.findAll().get(10).id();
		mockMvc.perform(delete("/api/todos/" + last)).andExpect(status().isNoContent());

		// The client is still asking for page 1, which no longer exists.
		mockMvc.perform(get("/api/todos").param("page", "1"))
				.andExpect(jsonPath("$.page").value(0))
				.andExpect(jsonPath("$.totalPages").value(1))
				.andExpect(jsonPath("$.items.length()").value(10));
	}
}
