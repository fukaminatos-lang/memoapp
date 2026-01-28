package com.example.demo.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.model.Account;

public interface AccountRepository extends JpaRepository<Account, Long> {
    // ユーザー名で検索するためのメソッド
    Optional<Account> findByUsername(String username);
}