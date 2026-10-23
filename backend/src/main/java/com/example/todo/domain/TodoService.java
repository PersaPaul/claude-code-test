package com.example.todo.domain;

import java.util.List;

import org.springframework.stereotype.Service;

@Service
public class TodoService {

	private final TodoRepository repository;

	public TodoService(TodoRepository repository) {
		this.repository = repository;
	}

	/**
	 * Returns the requested page, clamped into range: asking for a page past the
	 * end yields the last one, so deleting the final item on a page cannot strand
	 * the client on an empty view.
	 */
	public TodoPage list(TodoFilter filter, int page, int size) {
		List<Todo> matching = repository.findAll(filter);
		int totalPages = Math.max(1, (int) Math.ceil((double) matching.size() / size));
		int current = Math.min(page, totalPages - 1);

		int from = Math.min(current * size, matching.size());
		int to = Math.min(from + size, matching.size());

		return new TodoPage(matching.subList(from, to), current, size, matching.size(), totalPages,
				repository.countMatching(TodoFilter.ACTIVE), repository.countMatching(TodoFilter.COMPLETED));
	}

	public Todo create(String title) {
		return repository.create(title.trim());
	}

	/** Applies only the fields that were supplied; nulls mean "leave alone". */
	public Todo update(String id, String title, Boolean completed) {
		Todo todo = repository.findById(id).orElseThrow(() -> new TodoNotFoundException(id));
		if (title != null) {
			todo = todo.withTitle(title.trim());
		}
		if (completed != null) {
			todo = todo.withCompleted(completed);
		}
		return repository.save(todo);
	}

	public void delete(String id) {
		if (!repository.deleteById(id)) {
			throw new TodoNotFoundException(id);
		}
	}

	public int clearCompleted() {
		return repository.deleteCompleted().size();
	}
}
