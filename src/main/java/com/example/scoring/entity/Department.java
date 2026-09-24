package com.example.scoring.entity;
import jakarta.persistence.*;
@Entity public class Department {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id;
 @Column(nullable=false,unique=true) public String name;
 @ManyToOne public Department parent;
}
