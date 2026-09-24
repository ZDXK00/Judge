package com.example.scoring.repository;
import org.springframework.data.jpa.repository.JpaRepository; import com.example.scoring.entity.ScoreDetail;
public interface ScoreDetailRepository extends JpaRepository<ScoreDetail,Long>{ java.util.List<ScoreDetail> findBySubmissionId(Long id); }
