package com.example.scoring.repository;
import org.springframework.data.jpa.repository.JpaRepository; import com.example.scoring.entity.Department;
public interface DepartmentRepository extends JpaRepository<Department,Long>{  }
