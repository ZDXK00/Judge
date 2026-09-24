package com.example.scoring.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "score_task")
public class ScoreTask {

 @Id
 @GeneratedValue(strategy = GenerationType.IDENTITY)
 public Long id;

 @Column(nullable = false)
 public String name;

 @Column(nullable = false)
 public String status = "OPEN";

 @Enumerated(EnumType.STRING)
 @Column(nullable = false)
 public EvaluationDirection direction =
         EvaluationDirection.EMPLOYEE_TO_LEADER;

 @ManyToOne(optional = false)
 @JoinColumn(name = "target_leader_id")
 public User targetLeader;

 @ManyToOne(optional = false)
 @JoinColumn(name = "template_id")
 public ScoreTemplate template;
}