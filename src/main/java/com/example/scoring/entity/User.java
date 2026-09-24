package com.example.scoring.entity;
import jakarta.persistence.*;
@Entity @Table(name="app_user") public class User {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id;
 @Column(nullable=false) public String name;
 @Column(nullable=false,unique=true) public String username;
 @Column(nullable=false) public String role;
 @Column(nullable=false) public String password;
 @ManyToOne(optional=false) public Department department;
}
