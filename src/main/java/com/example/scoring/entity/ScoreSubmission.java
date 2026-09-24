package com.example.scoring.entity;
import jakarta.persistence.*; import java.time.*;
@Entity @Table(name="score_submission", uniqueConstraints=@UniqueConstraint(columnNames={"task_id","evaluator_id","target_id"}))
public class ScoreSubmission {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id;
 @ManyToOne(optional=false) public ScoreTask task;
 @ManyToOne(optional=false) public User evaluator;
 @ManyToOne public User target;
 @Column(nullable=false) public Integer totalScore;
 @Column(nullable=false) public LocalDateTime submittedAt;
}
