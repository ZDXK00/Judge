package com.example.scoring.common;


public class RoleCheck {


    public static boolean isAdmin(String role){

        return "ADMIN".equals(role);

    }


    public static boolean isLeader(String role){

        return "LEADER".equals(role);

    }


    public static boolean isEmployee(String role){

        return "EMPLOYEE".equals(role);

    }

}