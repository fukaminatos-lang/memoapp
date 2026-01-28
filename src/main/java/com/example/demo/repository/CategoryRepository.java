package com.example.demo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.model.Account;
import com.example.demo.model.Category;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    // ログインユーザーが作成したカテゴリ一覧を取得する
    List<Category> findByAccount(Account account);
}