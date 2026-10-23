package com.example.todo.domain;

import java.time.Instant;

/** A single todo item. Immutable: updates produce a new instance. */
public record Todo(String id, String title, boolean completed, Instant createdAt) {

	public Todo withTitle(String newTitle) {
		return new Todo(id, newTitle, completed, createdAt);
	}

	public Todo withCompleted(boolean newCompleted) {
		return new Todo(id, title, newCompleted, createdAt);
	}
}
