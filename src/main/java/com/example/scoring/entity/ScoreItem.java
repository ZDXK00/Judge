package com.example.scoring.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "score_item")
public class ScoreItem {

 @Id
 @GeneratedValue(strategy = GenerationType.IDENTITY)
 public Long id;

 /**
  * 数据库字段必须对应 question_text
  */
 @Column(name = "question_text", nullable = false)
 public String title;

 @Column(name = "max_score", nullable = false)
 public Integer maxScore = 10;

 @Column(name = "sort_order", nullable = false)
 public Integer sortOrder;

 @ManyToOne(optional = false)
 @JoinColumn(name = "template_id")
 public ScoreTemplate template;

 @ManyToOne
 @JoinColumn(name = "dimension_id")
 public UserDimension dimension;

 @Column(nullable = false)
 public boolean required = true;
}