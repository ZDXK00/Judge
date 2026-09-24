package com.example.scoring.entity;
import jakarta.persistence.*;
@Entity @Table(name="task_participant", uniqueConstraints=@UniqueConstraint(columnNames={"task_id","evaluator_id","target_id"}))
public class TaskParticipant {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id;
 @ManyToOne(optional=false) public ScoreTask task;
 @ManyToOne(optional=false) public User evaluator;
 @ManyToOne public User target;
 @Enumerated(EnumType.STRING) public EvaluationDirection direction;
 public boolean submitted=false;
}
