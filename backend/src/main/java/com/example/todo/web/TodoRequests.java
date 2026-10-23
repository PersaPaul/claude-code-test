package com.example.todo.web;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public final class TodoRequests {

	private TodoRequests() {
	}

	public record Create(@NotBlank(message = "title must not be blank") @Size(max = 200,
			message = "title must be at most 200 characters") String title) {
	}

	/** Both fields are optional; a null field is left untouched. */
	public record Update(@Size(min = 1, max = 200,
			message = "title must be between 1 and 200 characters") String title, Boolean completed) {
	}
}
