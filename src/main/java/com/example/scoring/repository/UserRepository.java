package com.example.scoring.repository;
import org.springframework.data.jpa.repository.JpaRepository; import com.example.scoring.entity.User;
public interface UserRepository extends JpaRepository<User,Long>{ java.util.List<User> findByDepartmentId(Long id); java.util.Optional<User> findByUsername(String username); }
