package com.example.todo.web;

import java.util.Locale;

import com.example.todo.domain.TodoFilter;

import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * The Vite dev server proxies /api, so CORS is not strictly needed, but allowing
 * the dev origin makes it possible to hit the API straight from localhost:5173.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

	/** Lets the API take ?filter=active rather than forcing Java's enum casing. */
	@Override
	public void addFormatters(FormatterRegistry registry) {
		registry.addConverter(String.class, TodoFilter.class,
				source -> TodoFilter.valueOf(source.trim().toUpperCase(Locale.ROOT)));
	}

	@Override
	public void addCorsMappings(CorsRegistry registry) {
		registry.addMapping("/api/**")
				.allowedOrigins("http://localhost:5173", "http://127.0.0.1:5173")
				.allowedMethods("GET", "POST", "PATCH", "DELETE");
	}
}
