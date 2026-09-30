package io.github.zxcbecause.taskmanager.task;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TaskRepository extends JpaRepository<Task, Long> {

    @Query("""
            select t from Task t
            where (:status is null or t.status = :status)
              and (:priority is null or t.priority = :priority)
              and lower(t.title) like :titlePattern
            """)
    Page<Task> search(@Param("status") TaskStatus status,
                      @Param("priority") TaskPriority priority,
                      @Param("titlePattern") String titlePattern,
                      Pageable pageable);

    long countByStatus(TaskStatus status);
}
