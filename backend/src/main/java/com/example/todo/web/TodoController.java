package com.example.todo.web;

import java.net.URI;
import java.util.Map;

import com.example.todo.domain.Todo;
import com.example.todo.domain.TodoFilter;
import com.example.todo.domain.TodoPage;
import com.example.todo.domain.TodoService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/todos")
public class TodoController {

	private final TodoService service;

	public TodoController(TodoService service) {
		this.service = service;
	}

	@GetMapping
	public TodoPage list(@RequestParam(defaultValue = "ALL") TodoFilter filter,
			@RequestParam(defaultValue = "0") @Min(value = 0, message = "page must not be negative") int page,
			@RequestParam(defaultValue = "10") @Min(value = 1, message = "size must be at least 1") @Max(
					value = 100, message = "size must be at most 100") int size) {
		return service.list(filter, page, size);
	}

	@PostMapping
	public ResponseEntity<Todo> create(@Valid @RequestBody TodoRequests.Create request) {
		Todo created = service.create(request.title());
		return ResponseEntity.created(URI.create("/api/todos/" + created.id())).body(created);
	}

	@PatchMapping("/{id}")
	public Todo update(@PathVariable String id, @Valid @RequestBody TodoRequests.Update request) {
		return service.update(id, request.title(), request.completed());
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> delete(@PathVariable String id) {
		service.delete(id);
		return ResponseEntity.noContent().build();
	}

	@DeleteMapping
	public Map<String, Integer> clearCompleted() {
		return Map.of("deleted", service.clearCompleted());
	}
}
