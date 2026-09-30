package io.github.zxcbecause.taskmanager.task.dto;

import io.github.zxcbecause.taskmanager.task.Task;
import io.github.zxcbecause.taskmanager.task.TaskPriority;
import io.github.zxcbecause.taskmanager.task.TaskStatus;

import java.time.Instant;
import java.time.LocalDate;

public record TaskResponse(
        Long id,
        String title,
        String description,
        TaskStatus status,
        TaskPriority priority,
        LocalDate dueDate,
        boolean overdue,
        Instant createdAt,
        Instant updatedAt
) {

    public static TaskResponse from(Task task, LocalDate today) {
        return new TaskResponse(
                task.getId(),
                task.getTitle(),
                task.getDescription(),
                task.getStatus(),
                task.getPriority(),
                task.getDueDate(),
                task.isOverdue(today),
                task.getCreatedAt(),
                task.getUpdatedAt());
    }
}
