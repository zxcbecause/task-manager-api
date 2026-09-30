package io.github.zxcbecause.taskmanager.task.dto;

import io.github.zxcbecause.taskmanager.task.TaskPriority;
import io.github.zxcbecause.taskmanager.task.TaskStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/**
 * Payload for creating or fully updating a task.
 * {@code status} is optional on create (defaults to TODO).
 */
public record TaskRequest(
        @NotBlank(message = "title is required")
        @Size(max = 120, message = "title must be at most 120 characters")
        String title,

        @Size(max = 2000, message = "description must be at most 2000 characters")
        String description,

        TaskStatus status,

        TaskPriority priority,

        LocalDate dueDate
) {
}
