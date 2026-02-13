package com.sorturlbackend.service;


import com.sorturlbackend.entity.User;

public interface UserService {
    User getUserById(Long id);
    User findByUsername(String name);
}
