package com.example.demo.repository;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.model.Account;
import com.example.demo.model.Memo;

public interface MemoRepository extends JpaRepository<Memo, Long> {
    List<Memo> findByAccount(Account account, Sort sort);
    
    List<Memo> findByAccountAndTitleContaining(Account account, String keyword, Sort sort);

    List<Memo> findByAccountAndCategoryId(Account account, Long categoryId, Sort sort);
    List<Memo> findByAccountAndCategoryIdAndTitleContaining(Account account, Long categoryId, String keyword, Sort sort);
}