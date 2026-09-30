package io.github.zxcbecause.taskmanager.task.dto;

import io.github.zxcbecause.taskmanager.task.TaskStatus;
import jakarta.validation.constraints.NotNull;

public record StatusUpdateRequest(
        @NotNull(message = "status is required")
        TaskStatus status
) {
}
