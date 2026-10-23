package com.example.todo.domain;

import java.util.function.Predicate;

public enum TodoFilter {

	ALL(todo -> true),
	ACTIVE(todo -> !todo.completed()),
	COMPLETED(Todo::completed);

	private final Predicate<Todo> predicate;

	TodoFilter(Predicate<Todo> predicate) {
		this.predicate = predicate;
	}

	public Predicate<Todo> predicate() {
		return this.predicate;
	}
}
