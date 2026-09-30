package io.github.zxcbecause.taskmanager.task;

import io.github.zxcbecause.taskmanager.task.dto.TaskRequest;
import io.github.zxcbecause.taskmanager.task.dto.TaskResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    private static final Clock FIXED = Clock.fixed(Instant.parse("2026-09-15T10:00:00Z"), ZoneOffset.UTC);

    @Mock
    private TaskRepository repository;

    private TaskService service;

    @BeforeEach
    void setUp() {
        service = new TaskService(repository, FIXED);
    }

    @Test
    void createTrimsTitleAndDefaultsToTodoAndMedium() {
        when(repository.save(any(Task.class))).thenAnswer(inv -> inv.getArgument(0));

        TaskResponse response = service.create(new TaskRequest("  Write README  ", null, null, null, null));

        ArgumentCaptor<Task> captor = ArgumentCaptor.forClass(Task.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getTitle()).isEqualTo("Write README");
        assertThat(response.status()).isEqualTo(TaskStatus.TODO);
        assertThat(response.priority()).isEqualTo(TaskPriority.MEDIUM);
    }

    @Test
    void overdueIsCalculatedFromClock() {
        Task task = new Task("Pay bills", null, TaskPriority.HIGH, LocalDate.of(2026, 9, 14));
        when(repository.findById(1L)).thenReturn(Optional.of(task));

        assertThat(service.get(1L).overdue()).isTrue();

        task.setStatus(TaskStatus.DONE);
        assertThat(service.get(1L).overdue()).isFalse();
    }

    @Test
    void taskDueTodayIsNotOverdue() {
        Task task = new Task("Today", null, null, LocalDate.of(2026, 9, 15));
        when(repository.findById(2L)).thenReturn(Optional.of(task));

        assertThat(service.get(2L).overdue()).isFalse();
    }

    @Test
    void getUnknownTaskThrows() {
        when(repository.findById(42L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.get(42L))
                .isInstanceOf(TaskNotFoundException.class)
                .hasMessageContaining("42");
    }

    @Test
    void changeStatusUpdatesTask() {
        Task task = new Task("Deploy", null, null, null);
        when(repository.findById(3L)).thenReturn(Optional.of(task));
        when(repository.saveAndFlush(task)).thenReturn(task);

        TaskResponse response = service.changeStatus(3L, TaskStatus.IN_PROGRESS);

        assertThat(response.status()).isEqualTo(TaskStatus.IN_PROGRESS);
    }

    @Test
    void deleteUnknownTaskThrowsAndDoesNotDelete() {
        when(repository.existsById(7L)).thenReturn(false);

        assertThatThrownBy(() -> service.delete(7L)).isInstanceOf(TaskNotFoundException.class);
        verify(repository, never()).deleteById(any());
    }
}
