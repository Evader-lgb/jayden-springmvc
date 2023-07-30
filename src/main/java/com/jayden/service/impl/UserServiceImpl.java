package com.jayden.service.impl;

import com.jayden.pojo.User;
import com.jayden.service.UserService;
import com.springmvc.annotation.Service;

@Service(value="userService")
public class UserServiceImpl implements UserService {


    @Override
    public  void  findUser(){
        System.out.println("====调用UserServiceImpl==findUser===");
    }

    @Override
    public User getUser(){

       return new User(1,"老王","admin");
    }

}
