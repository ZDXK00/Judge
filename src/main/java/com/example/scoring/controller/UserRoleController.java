package com.example.scoring.controller;


import com.example.scoring.entity.User;
import com.example.scoring.repository.UserRepository;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/user")
public class UserRoleController {


    private final UserRepository users;


    public UserRoleController(
            UserRepository users
    ){

        this.users=users;

    }



    @GetMapping("/{id}")
    public User getUser(
            @PathVariable Long id
    ){


        return users.findById(id)
                .orElseThrow(
                        ()->new RuntimeException("用户不存在")
                );

    }

}