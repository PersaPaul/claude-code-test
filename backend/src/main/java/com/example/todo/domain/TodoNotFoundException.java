package com.example.todo.domain;

public class TodoNotFoundException extends RuntimeException {

	public TodoNotFoundException(String id) {
		super("No todo with id " + id);
	}
}
