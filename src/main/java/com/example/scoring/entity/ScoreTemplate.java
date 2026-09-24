package com.example.scoring.entity;

import jakarta.persistence.*;

@Entity
public class ScoreTemplate {

 @Id
 @GeneratedValue(strategy = GenerationType.IDENTITY)
 public Long id;

 @Column(nullable = false)
 public String name;

 @Enumerated(EnumType.STRING)
 @Column(nullable = false)
 public EvaluationDirection direction =
         EvaluationDirection.EMPLOYEE_TO_LEADER;

 @Column(nullable = false)
 public Integer totalScore = 100;

 @Column(nullable = false)
 public Integer questionCount = 10;

 public boolean custom;
}