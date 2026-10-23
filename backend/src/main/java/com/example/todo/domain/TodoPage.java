package com.example.todo.domain;

import java.util.List;

/**
 * One page of todos plus the totals the UI needs. {@code activeCount} and
 * {@code completedCount} span every todo, not just this page or filter, so the
 * footer stays correct while the client only holds one page.
 *
 * @param totalItems number of todos matching the requested filter
 * @param totalPages always at least 1, so an empty list still reads "1 of 1"
 */
public record TodoPage(List<Todo> items, int page, int size, long totalItems, int totalPages,
		long activeCount, long completedCount) {
}
