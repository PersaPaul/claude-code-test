package com.example.todo.domain;

import java.time.Instant;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.stereotype.Repository;

/**
 * In-memory store, kept in insertion order. Swap this for a JPA repository when
 * the todos need to outlive the process.
 */
@Repository
public class TodoRepository {

	private final Map<String, Todo> todos = new ConcurrentHashMap<>();

	private final Map<String, Long> ordering = new ConcurrentHashMap<>();

	private final AtomicLong sequence = new AtomicLong();

	public List<Todo> findAll() {
		return todos.values().stream().sorted(byInsertion()).toList();
	}

	public List<Todo> findAll(TodoFilter filter) {
		return todos.values().stream().filter(filter.predicate()).sorted(byInsertion()).toList();
	}

	public long countMatching(TodoFilter filter) {
		return todos.values().stream().filter(filter.predicate()).count();
	}

	private Comparator<Todo> byInsertion() {
		return Comparator.comparingLong(todo -> ordering.getOrDefault(todo.id(), 0L));
	}

	public Optional<Todo> findById(String id) {
		return Optional.ofNullable(todos.get(id));
	}

	public Todo create(String title) {
		Todo todo = new Todo(UUID.randomUUID().toString(), title, false, Instant.now());
		ordering.put(todo.id(), sequence.incrementAndGet());
		todos.put(todo.id(), todo);
		return todo;
	}

	public Todo save(Todo todo) {
		todos.put(todo.id(), todo);
		return todo;
	}

	public boolean deleteById(String id) {
		ordering.remove(id);
		return todos.remove(id) != null;
	}

	/** Removes every completed todo and returns the ones that were removed. */
	public Collection<Todo> deleteCompleted() {
		List<Todo> completed = todos.values().stream().filter(Todo::completed).toList();
		completed.forEach(todo -> deleteById(todo.id()));
		return completed;
	}
}
