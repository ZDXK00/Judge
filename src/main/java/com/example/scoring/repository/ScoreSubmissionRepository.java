package com.example.scoring.repository;
import org.springframework.data.jpa.repository.JpaRepository; import com.example.scoring.entity.ScoreSubmission;
public interface ScoreSubmissionRepository extends JpaRepository<ScoreSubmission,Long>{ boolean existsByTaskIdAndEvaluatorId(Long taskId,Long evaluatorId); java.util.List<ScoreSubmission> findByTaskId(Long id); }
