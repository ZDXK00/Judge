package com.example.scoring.repository;

import com.example.scoring.entity.TaskParticipant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TaskParticipantRepository
        extends JpaRepository<TaskParticipant, Long> {

    List<TaskParticipant> findByTaskId(Long taskId);

    boolean existsByTaskIdAndEvaluatorId(
            Long taskId,
            Long evaluatorId
    );

    boolean existsByTaskIdAndEvaluatorIdAndTargetId(
            Long taskId,
            Long evaluatorId,
            Long targetId
    );
}