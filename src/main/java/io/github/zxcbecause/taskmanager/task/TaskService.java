package io.github.zxcbecause.taskmanager.task;

import io.github.zxcbecause.taskmanager.task.dto.TaskRequest;
import io.github.zxcbecause.taskmanager.task.dto.TaskResponse;
import io.github.zxcbecause.taskmanager.task.dto.TaskStats;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Locale;

@Service
@Transactional(readOnly = true)
public class TaskService {

    private final TaskRepository repository;
    private final Clock clock;

    public TaskService(TaskRepository repository, Clock clock) {
        this.repository = repository;
        this.clock = clock;
    }

    public Page<TaskResponse> search(TaskStatus status, TaskPriority priority, String search, Pageable pageable) {
        String pattern = "%" + (search == null ? "" : search.trim().toLowerCase(Locale.ROOT)) + "%";
        LocalDate today = today();
        return repository.search(status, priority, pattern, pageable)
                .map(task -> TaskResponse.from(task, today));
    }

    public TaskResponse get(Long id) {
        return TaskResponse.from(find(id), today());
    }

    @Transactional
    public TaskResponse create(TaskRequest request) {
        Task task = new Task(request.title().trim(), request.description(), request.priority(), request.dueDate());
        if (request.status() != null) {
            task.setStatus(request.status());
        }
        return TaskResponse.from(repository.save(task), today());
    }

    @Transactional
    public TaskResponse update(Long id, TaskRequest request) {
        Task task = find(id);
        task.setTitle(request.title().trim());
        task.setDescription(request.description());
        task.setDueDate(request.dueDate());
        if (request.priority() != null) {
            task.setPriority(request.priority());
        }
        if (request.status() != null) {
            task.setStatus(request.status());
        }
        return TaskResponse.from(repository.saveAndFlush(task), today());
    }

    @Transactional
    public TaskResponse changeStatus(Long id, TaskStatus status) {
        Task task = find(id);
        task.setStatus(status);
        return TaskResponse.from(repository.saveAndFlush(task), today());
    }

    @Transactional
    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new TaskNotFoundException(id);
        }
        repository.deleteById(id);
    }

    public TaskStats stats() {
        return new TaskStats(
                repository.count(),
                repository.countByStatus(TaskStatus.TODO),
                repository.countByStatus(TaskStatus.IN_PROGRESS),
                repository.countByStatus(TaskStatus.DONE));
    }

    private Task find(Long id) {
        return repository.findById(id).orElseThrow(() -> new TaskNotFoundException(id));
    }

    private LocalDate today() {
        return LocalDate.now(clock);
    }
}
