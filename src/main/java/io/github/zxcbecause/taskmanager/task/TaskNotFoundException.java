package io.github.zxcbecause.taskmanager.task;

public class TaskNotFoundException extends RuntimeException {

    public TaskNotFoundException(Long id) {
        super("Task " + id + " not found");
    }
}
