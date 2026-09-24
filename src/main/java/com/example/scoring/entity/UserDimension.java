package com.example.scoring.entity;
import jakarta.persistence.*;
@Entity @Table(name="user_dimension", uniqueConstraints=@UniqueConstraint(columnNames={"user_id","dimension_no"}))
public class UserDimension {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id;
 @ManyToOne(optional=false) public User user;
 @Column(name="dimension_no", nullable=false) public Integer dimensionNo;
 @Column(nullable=false) public String dimensionName;
 @Column(nullable=false) public String dimensionType;
 public boolean enabled=true;
}
