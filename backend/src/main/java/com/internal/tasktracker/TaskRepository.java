package com.internal.tasktracker;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    // AND binds tighter than OR, so the title/description match must be parenthesised;
    // otherwise archived rows leak in and the status filter only applies to the description branch.
    String SEARCH_WHERE = "WHERE archived = FALSE "
            + "AND (LOWER(title) LIKE :term OR LOWER(description) LIKE :term) "
            + "AND (:status IS NULL OR status = :status)";

    // Search tasks by term and optional status filter, paginated in the database.
    // id is a tie-breaker so page boundaries are stable when created_at values collide.
    @Query(value = "SELECT * FROM tasks " + SEARCH_WHERE + " ORDER BY created_at DESC, id DESC",
           countQuery = "SELECT COUNT(*) FROM tasks " + SEARCH_WHERE,
           nativeQuery = true)
    Page<Task> searchTasks(@Param("term") String term, @Param("status") String status, Pageable pageable);
}
