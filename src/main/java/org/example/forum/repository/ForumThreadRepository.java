package org.example.forum.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.example.forum.model.ForumThread;

import java.util.Optional;

@Repository
public interface ForumThreadRepository extends JpaRepository<ForumThread, Long> {
    Page<ForumThread> findAllByOrderByIdDesc(Pageable pageable);
}
