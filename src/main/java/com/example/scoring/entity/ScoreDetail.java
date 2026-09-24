package com.example.scoring.entity;
import jakarta.persistence.*;
@Entity public class ScoreDetail {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id;
 @ManyToOne(optional=false) public ScoreSubmission submission;
 @ManyToOne(optional=false) public ScoreItem item;
 @ManyToOne public UserDimension dimension;
 @Column(nullable=false) public Integer score;
}
