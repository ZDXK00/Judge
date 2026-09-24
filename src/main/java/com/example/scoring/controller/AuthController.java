package com.example.scoring.controller;


import com.example.scoring.entity.User;
import com.example.scoring.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;


@RestController
@RequestMapping("/api/auth")
public class AuthController {


 private final UserRepository users;


 private final BCryptPasswordEncoder encoder =
         new BCryptPasswordEncoder();



 public AuthController(
         UserRepository users
 ){

  this.users = users;

 }



 @PostMapping("/login")
 public Map<String,Object> login(
         @RequestBody LoginRequest request
 ){


  User user =
          users.findByUsername(request.username())
                  .orElseThrow(
                          ()->new RuntimeException("用户不存在")
                  );



  if(!encoder.matches(
          request.password(),
          user.password
  )){

   throw new RuntimeException("密码错误");

  }



  Map<String,Object> result =
          new HashMap<>();


  result.put("id",user.id);

  result.put("username",
          user.username);

  result.put("name",
          user.name);


  result.put("role",
          user.role);



  return result;

 }



 public record LoginRequest(
         String username,
         String password
 ){}



}