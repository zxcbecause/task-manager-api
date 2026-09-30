package io.github.zxcbecause.taskmanager.task;

import io.github.zxcbecause.taskmanager.task.dto.StatusUpdateRequest;
import io.github.zxcbecause.taskmanager.task.dto.TaskRequest;
import io.github.zxcbecause.taskmanager.task.dto.TaskResponse;
import io.github.zxcbecause.taskmanager.task.dto.TaskStats;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/api/tasks")
@Tag(name = "Tasks", description = "Create, read, update and delete tasks")
public class TaskController {

    private final TaskService service;

    public TaskController(TaskService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "List tasks with optional filters, pagination and sorting")
    public Page<TaskResponse> list(@RequestParam(required = false) TaskStatus status,
                                   @RequestParam(required = false) TaskPriority priority,
                                   @RequestParam(required = false) String search,
                                   @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
                                   Pageable pageable) {
        return service.search(status, priority, search, pageable);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a task by id")
    public TaskResponse get(@PathVariable Long id) {
        return service.get(id);
    }

    @GetMapping("/stats")
    @Operation(summary = "Number of tasks per status")
    public TaskStats stats() {
        return service.stats();
    }

    @PostMapping
    @Operation(summary = "Create a task")
    public ResponseEntity<TaskResponse> create(@Valid @RequestBody TaskRequest request) {
        TaskResponse created = service.create(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.id())
                .toUri();
        return ResponseEntity.created(location).body(created);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Replace a task")
    public TaskResponse update(@PathVariable Long id, @Valid @RequestBody TaskRequest request) {
        return service.update(id, request);
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Change only the status of a task")
    public TaskResponse changeStatus(@PathVariable Long id, @Valid @RequestBody StatusUpdateRequest request) {
        return service.changeStatus(id, request.status());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete a task")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
