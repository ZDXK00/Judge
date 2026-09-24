package com.example.scoring.repository;
import org.springframework.data.jpa.repository.JpaRepository; import com.example.scoring.entity.ScoreTask;
public interface ScoreTaskRepository extends JpaRepository<ScoreTask,Long>{  }
