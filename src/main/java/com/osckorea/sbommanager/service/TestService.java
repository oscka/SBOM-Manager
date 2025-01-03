package com.osckorea.sbommanager.service;


import com.osckorea.sbommanager.domian.entity.User;
import com.osckorea.sbommanager.repository.TestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TestService {
    private final TestRepository testUserRepository;

    @Transactional
    public List<User> getAllUsers() {
        return testUserRepository.getAllUsers();
    }

    @Transactional
    public Iterable<User> getAllUsers02() {
        return testUserRepository.findAll();
    }

    @Transactional
    public User createUser(User user) {
        user.setCreatedAt(LocalDateTime.now());
        return testUserRepository.save(user);
    }

    @Transactional
    public User editUser(Long id, User user) {
        user.setId(id);
        return testUserRepository.save(user);
    }

    @Transactional
    public void deleteUser(Long id) {
        testUserRepository.deleteById(id);
    }

}