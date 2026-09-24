package com.example.scoring.repository;
import com.example.scoring.entity.*; import org.springframework.data.jpa.repository.JpaRepository; import java.util.*;
public interface UserDimensionRepository extends JpaRepository<UserDimension,Long>{ List<UserDimension> findByUserIdOrderByDimensionNo(Long userId); }
