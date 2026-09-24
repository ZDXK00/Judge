package com.example.scoring.repository;

import com.example.scoring.entity.ScoreItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ScoreItemRepository extends JpaRepository<ScoreItem, Long> {

    List<ScoreItem> findByTemplateIdOrderBySortOrder(Long templateId);
}